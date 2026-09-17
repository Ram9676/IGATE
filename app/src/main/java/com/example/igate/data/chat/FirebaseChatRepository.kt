package com.example.igate.data.chat

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// ── Data Models ──

data class ChatRoom(
    val id: String = "",
    val name: String = "",
    val participants: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val participantRoles: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastTimestamp: Long = 0L,
    val roomType: String = "DM", // DM, GROUP, AI_ASSISTANT
    val unreadCount: Map<String, Int> = emptyMap()
)

data class RealtimeChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderRole: String = "STUDENT",
    val text: String = "",
    val timestamp: Long = 0L,
    val isRead: Boolean = false
)

// ── Repository ──

class FirebaseChatRepository {

    private val db = FirebaseFirestore.getInstance()
    private val rtdb = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val currentUid: String?
        get() = auth.currentUser?.uid

    // ── Chat Rooms (Firestore) ──

    fun getChatRooms(): Flow<List<ChatRoom>> = callbackFlow {
        val uid = currentUid
        if (uid == null) {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }
        val listener = db.collection("chat_rooms")
            .whereArrayContains("participants", uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val rooms = snapshot?.documents?.mapNotNull {
                    it.toObject(ChatRoom::class.java)?.copy(id = it.id)
                }?.sortedByDescending { it.lastTimestamp } ?: emptyList()
                trySend(rooms)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createChatRoom(
        otherUid: String,
        otherName: String,
        otherRole: String,
        myName: String,
        myRole: String
    ): String {
        val uid = currentUid ?: throw IllegalStateException("Not signed in")

        // Check for existing DM
        val existing = db.collection("chat_rooms")
            .whereArrayContains("participants", uid)
            .whereEqualTo("roomType", "DM")
            .get().await()

        val existingRoom = existing.documents.firstOrNull { doc ->
            val participants = doc.get("participants") as? List<*>
            participants?.contains(otherUid) == true
        }

        if (existingRoom != null) return existingRoom.id

        val room = ChatRoom(
            name = "$myName & $otherName",
            participants = listOf(uid, otherUid),
            participantNames = mapOf(uid to myName, otherUid to otherName),
            participantRoles = mapOf(uid to myRole, otherUid to otherRole),
            lastMessage = "Chat started",
            lastTimestamp = System.currentTimeMillis(),
            roomType = "DM"
        )
        val docRef = db.collection("chat_rooms").add(room).await()
        return docRef.id
    }

    suspend fun createGroupChatRoom(
        name: String,
        participantIds: List<String>,
        participantNamesMap: Map<String, String>,
        participantRolesMap: Map<String, String>
    ): String {
        val room = ChatRoom(
            name = name,
            participants = participantIds,
            participantNames = participantNamesMap,
            participantRoles = participantRolesMap,
            lastMessage = "Group created",
            lastTimestamp = System.currentTimeMillis(),
            roomType = "GROUP"
        )
        val docRef = db.collection("chat_rooms").add(room).await()
        return docRef.id
    }

    // ── Messages (Realtime Database — low latency) ──

    fun getMessages(roomId: String): Flow<List<RealtimeChatMessage>> = callbackFlow {
        val ref = rtdb.reference.child("messages").child(roomId)
            .orderByChild("timestamp")
            .limitToLast(100)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<RealtimeChatMessage>()
                for (child in snapshot.children) {
                    val msg = child.getValue(RealtimeChatMessage::class.java)
                    if (msg != null) {
                        messages.add(msg.copy(id = child.key ?: ""))
                    }
                }
                trySend(messages)
            }
            override fun onCancelled(error: DatabaseError) {
                trySend(emptyList())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun sendMessage(roomId: String, text: String, senderName: String, senderRole: String) {
        val uid = currentUid ?: return
        val ref = rtdb.reference.child("messages").child(roomId).push()
        val message = mapOf(
            "senderId" to uid,
            "senderName" to senderName,
            "senderRole" to senderRole,
            "text" to text,
            "timestamp" to ServerValue.TIMESTAMP,
            "isRead" to false
        )
        ref.setValue(message).await()

        // Update last message in Firestore chat room
        db.collection("chat_rooms").document(roomId).update(
            "lastMessage", text,
            "lastTimestamp", System.currentTimeMillis()
        )
    }

    // ── Typing Indicator (Realtime Database presence) ──

    fun setTyping(roomId: String, isTyping: Boolean) {
        val uid = currentUid ?: return
        val ref = rtdb.reference.child("typing").child(roomId).child(uid)
        if (isTyping) {
            ref.setValue(true)
            ref.onDisconnect().removeValue()
        } else {
            ref.removeValue()
        }
    }

    fun getTypingUsers(roomId: String): Flow<List<String>> = callbackFlow {
        val ref = rtdb.reference.child("typing").child(roomId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val typingUsers = mutableListOf<String>()
                for (child in snapshot.children) {
                    if (child.getValue(Boolean::class.java) == true && child.key != currentUid) {
                        typingUsers.add(child.key ?: "")
                    }
                }
                trySend(typingUsers)
            }
            override fun onCancelled(error: DatabaseError) {
                trySend(emptyList())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // ── Seed demo chat rooms ──

    suspend fun seedDemoChatRoomsIfEmpty() {
        val uid = currentUid ?: return
        val existing = db.collection("chat_rooms")
            .whereArrayContains("participants", uid)
            .get().await()
        if (existing.isEmpty) {
            // Create demo rooms
            val demoRooms = listOf(
                ChatRoom(
                    name = "Prof. Arvind Rao",
                    participants = listOf(uid, "demo_teacher"),
                    participantNames = mapOf(uid to "Student", "demo_teacher" to "Prof. Arvind Rao"),
                    participantRoles = mapOf(uid to "STUDENT", "demo_teacher" to "TEACHER"),
                    lastMessage = "Welcome to IGATE! Feel free to ask doubts.",
                    lastTimestamp = System.currentTimeMillis() - 3600000,
                    roomType = "DM"
                ),
                ChatRoom(
                    name = "GATE CS Alpha Batch",
                    participants = listOf(uid, "demo_teacher", "demo_admin"),
                    participantNames = mapOf(uid to "Student", "demo_teacher" to "Prof. Arvind Rao", "demo_admin" to "Admin"),
                    participantRoles = mapOf(uid to "STUDENT", "demo_teacher" to "TEACHER", "demo_admin" to "ADMIN"),
                    lastMessage = "Next mock test is on Sunday 10 AM",
                    lastTimestamp = System.currentTimeMillis() - 1800000,
                    roomType = "GROUP"
                ),
                ChatRoom(
                    name = "Admin Support",
                    participants = listOf(uid, "demo_admin"),
                    participantNames = mapOf(uid to "Student", "demo_admin" to "Chief Academic Director"),
                    participantRoles = mapOf(uid to "STUDENT", "demo_admin" to "ADMIN"),
                    lastMessage = "Your batch enrollment is confirmed.",
                    lastTimestamp = System.currentTimeMillis() - 7200000,
                    roomType = "DM"
                )
            )
            for (room in demoRooms) {
                val docRef = db.collection("chat_rooms").add(room).await()
                // Seed a welcome message in Realtime DB
                val msgRef = rtdb.reference.child("messages").child(docRef.id).push()
                msgRef.setValue(mapOf(
                    "senderId" to room.participants.last(),
                    "senderName" to room.participantNames[room.participants.last()],
                    "senderRole" to room.participantRoles[room.participants.last()],
                    "text" to room.lastMessage,
                    "timestamp" to ServerValue.TIMESTAMP,
                    "isRead" to false
                ))
            }
        }
    }
}
