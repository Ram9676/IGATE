package com.example.igate.data.chat

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import android.util.Log

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
    private val auth = FirebaseAuth.getInstance()

    private val currentUid: String
        get() = auth.currentUser?.uid ?: "demo_student"

    private val currentName: String
        get() = auth.currentUser?.displayName ?: auth.currentUser?.email?.substringBefore('@') ?: "Rachel Green"

    private val defaultRooms: List<ChatRoom>
        get() = listOf(
            ChatRoom(
                id = "room_faculty",
                name = "Prof. Arvind Rao (Senior Physics Faculty)",
                participants = listOf(currentUid, "faculty_1"),
                participantNames = mapOf(currentUid to currentName, "faculty_1" to "Prof. Arvind Rao"),
                participantRoles = mapOf(currentUid to "STUDENT", "faculty_1" to "TEACHER"),
                lastMessage = "Welcome to IGATE! Feel free to ask any doubt on Mechanics.",
                lastTimestamp = System.currentTimeMillis() - 3600000,
                roomType = "DM"
            ),
            ChatRoom(
                id = "room_batch",
                name = "GATE 2027 CS - Alpha Star Group",
                participants = listOf(currentUid, "faculty_1", "admin_1"),
                participantNames = mapOf(currentUid to currentName, "faculty_1" to "Prof. Arvind Rao", "admin_1" to "IGATE Academic Coordinator"),
                participantRoles = mapOf(currentUid to "STUDENT", "faculty_1" to "TEACHER", "admin_1" to "ADMIN"),
                lastMessage = "The All India Mock Test 1 results will be released tomorrow at 6 PM.",
                lastTimestamp = System.currentTimeMillis() - 1800000,
                roomType = "GROUP"
            ),
            ChatRoom(
                id = "room_support",
                name = "IGATE Student Desk & Counseling",
                participants = listOf(currentUid, "admin_1"),
                participantNames = mapOf(currentUid to currentName, "admin_1" to "Student Counselor"),
                participantRoles = mapOf(currentUid to "STUDENT", "admin_1" to "ADMIN"),
                lastMessage = "Your digital study library has been unlocked successfully.",
                lastTimestamp = System.currentTimeMillis() - 7200000,
                roomType = "DM"
            )
        )

    private val defaultMessages = mapOf(
        "room_faculty" to listOf(
            RealtimeChatMessage("m1", "faculty_1", "Prof. Arvind Rao", "TEACHER", "Hello! Make sure to review the eigenvalues proof before tomorrow's class.", System.currentTimeMillis() - 3600000, true),
            RealtimeChatMessage("m2", currentUid, currentName, "STUDENT", "Yes Professor, I have completed the lecture and formula handbook.", System.currentTimeMillis() - 1800000, true),
            RealtimeChatMessage("m3", "faculty_1", "Prof. Arvind Rao", "TEACHER", "Excellent. Feel free to post any questions in our doubts section.", System.currentTimeMillis() - 600000, true)
        ),
        "room_batch" to listOf(
            RealtimeChatMessage("b1", "faculty_1", "Prof. Arvind Rao", "TEACHER", "Welcome all to the GATE 2027 Alpha Star Batch!", System.currentTimeMillis() - 86400000, true),
            RealtimeChatMessage("b2", "admin_1", "IGATE Coordinator", "ADMIN", "Schedule updated: Algorithms class daily at 7 AM.", System.currentTimeMillis() - 43200000, true)
        ),
        "room_support" to listOf(
            RealtimeChatMessage("s1", "admin_1", "Student Counselor", "ADMIN", "Your digital study library has been unlocked successfully. Best of luck for GATE!", System.currentTimeMillis() - 7200000, true)
        )
    )

    // ── Chat Rooms (Firestore with rich fallback) ──

    fun getChatRooms(): Flow<List<ChatRoom>> = callbackFlow {
        val uid = currentUid
        trySend(defaultRooms)

        val listener = db.collection("chat_rooms")
            .whereArrayContains("participants", uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("ChatRepo", "Error fetching rooms: ${e.message}")
                    trySend(defaultRooms)
                    return@addSnapshotListener
                }
                val rooms = snapshot?.documents?.mapNotNull {
                    runCatching { it.toObject(ChatRoom::class.java)?.copy(id = it.id) }.getOrNull()
                }?.sortedByDescending { it.lastTimestamp } ?: emptyList()

                trySend(if (rooms.isNotEmpty()) rooms else defaultRooms)
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
        val uid = currentUid
        try {
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
        } catch (e: Exception) {
            Log.w("ChatRepo", "createChatRoom error: ${e.message}")
            return "room_faculty"
        }
    }

    suspend fun createGroupChatRoom(
        name: String,
        participantIds: List<String>,
        participantNamesMap: Map<String, String>,
        participantRolesMap: Map<String, String>
    ): String {
        try {
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
        } catch (e: Exception) {
            Log.w("ChatRepo", "createGroupChatRoom error: ${e.message}")
            return "room_batch"
        }
    }

    // ── Messages (Firestore Subcollection with Realtime Updates) ──

    fun getMessages(roomId: String): Flow<List<RealtimeChatMessage>> = callbackFlow {
        val fallback = defaultMessages[roomId] ?: listOf(
            RealtimeChatMessage("init", "system", "IGATE Assistant", "ADMIN", "Welcome to $roomId! Start typing below.", System.currentTimeMillis(), true)
        )
        trySend(fallback)

        val listener = db.collection("chat_rooms").document(roomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("ChatRepo", "getMessages error: ${e.message}")
                    trySend(fallback)
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    runCatching { doc.toObject(RealtimeChatMessage::class.java)?.copy(id = doc.id) }.getOrNull()
                } ?: emptyList()

                trySend(if (messages.isNotEmpty()) messages else fallback)
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(roomId: String, text: String, senderName: String, senderRole: String) {
        val uid = currentUid
        val message = RealtimeChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            senderId = uid,
            senderName = senderName,
            senderRole = senderRole,
            text = text,
            timestamp = System.currentTimeMillis(),
            isRead = true
        )

        try {
            db.collection("chat_rooms").document(roomId)
                .collection("messages")
                .add(message).await()

            db.collection("chat_rooms").document(roomId).update(
                "lastMessage", text,
                "lastTimestamp", System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.w("ChatRepo", "sendMessage error: ${e.message}")
        }
    }

    // ── Typing Indicator (In-Memory / Firestore Presence) ──

    private val typingStateFlow = kotlinx.coroutines.flow.MutableStateFlow<List<String>>(emptyList())

    fun setTyping(roomId: String, isTyping: Boolean) {
        // Safe in-memory presence tracking
        if (isTyping) {
            typingStateFlow.value = listOf("Typing...")
        } else {
            typingStateFlow.value = emptyList()
        }
    }

    fun getTypingUsers(roomId: String): Flow<List<String>> = typingStateFlow

    // ── Seed demo chat rooms ──

    suspend fun seedDemoChatRoomsIfEmpty() {
        val uid = currentUid
        try {
            val existing = db.collection("chat_rooms")
                .whereArrayContains("participants", uid)
                .get().await()
            if (existing.isEmpty) {
                for (room in defaultRooms) {
                    val docRef = db.collection("chat_rooms").document(room.id)
                    docRef.set(room).await()

                    val msgs = defaultMessages[room.id] ?: emptyList()
                    for (msg in msgs) {
                        docRef.collection("messages").document(msg.id).set(msg)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("ChatRepo", "seedDemoChatRoomsIfEmpty error: ${e.message}")
        }
    }
}
