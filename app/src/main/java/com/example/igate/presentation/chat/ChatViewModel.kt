package com.example.igate.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.igate.data.analytics.AnalyticsTracker
import com.example.igate.data.chat.ChatRoom
import com.example.igate.data.chat.FirebaseChatRepository
import com.example.igate.data.chat.RealtimeChatMessage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val chatRepo = FirebaseChatRepository()
    private val auth = FirebaseAuth.getInstance()

    val chatRooms: StateFlow<List<ChatRoom>> = chatRepo.getChatRooms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentRoomId = MutableStateFlow<String?>(null)
    val currentRoomId: StateFlow<String?> = _currentRoomId.asStateFlow()

    private val _messages = MutableStateFlow<List<RealtimeChatMessage>>(emptyList())
    val messages: StateFlow<List<RealtimeChatMessage>> = _messages.asStateFlow()

    private val _typingUsers = MutableStateFlow<List<String>>(emptyList())
    val typingUsers: StateFlow<List<String>> = _typingUsers.asStateFlow()

    init {
        viewModelScope.launch {
            try { chatRepo.seedDemoChatRoomsIfEmpty() } catch (_: Exception) { }
        }
    }

    fun openRoom(roomId: String) {
        _currentRoomId.value = roomId
        viewModelScope.launch {
            try {
                chatRepo.getMessages(roomId).collect { _messages.value = it }
            } catch (e: Exception) {
                // Safe catch
            }
        }
        viewModelScope.launch {
            try {
                chatRepo.getTypingUsers(roomId).collect { _typingUsers.value = it }
            } catch (e: Exception) {
                // Safe catch
            }
        }
    }

    fun sendMessage(text: String, senderName: String, senderRole: String) {
        val roomId = _currentRoomId.value ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            try {
                chatRepo.sendMessage(roomId, text.trim(), senderName, senderRole)
                AnalyticsTracker.logChatMessageSent(roomId, senderRole)
            } catch (_: Exception) { }
        }
    }

    fun setTyping(isTyping: Boolean) {
        val roomId = _currentRoomId.value ?: return
        chatRepo.setTyping(roomId, isTyping)
    }

    fun closeRoom() {
        val roomId = _currentRoomId.value ?: return
        chatRepo.setTyping(roomId, false)
        _currentRoomId.value = null
        _messages.value = emptyList()
    }

    val currentUserId: String?
        get() = auth.currentUser?.uid
}
