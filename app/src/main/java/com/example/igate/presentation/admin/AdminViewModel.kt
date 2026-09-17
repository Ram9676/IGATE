package com.example.igate.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.igate.domain.model.*
import com.example.igate.domain.repository.IgateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class UserRosterItem(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val branch: String,
    val enrollmentStatus: String
)

class AdminViewModel(
    private val repository: IgateRepository
) : ViewModel() {

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    val batches: StateFlow<List<Batch>> = repository.getBatches()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _usersRoster = MutableStateFlow(
        listOf(
            UserRosterItem("u1", "Rachel Green", "rachel@igate.com", UserRole.STUDENT, "CS/IT", "Enrolled (GATE 2027)"),
            UserRosterItem("u2", "Prof. Arvind Rao", "arvind.rao@igate.com", UserRole.TEACHER, "CS/IT", "Faculty (Algorithms & Math)"),
            UserRosterItem("u3", "Dr. Meenakshi Sundaram", "meenakshi@igate.com", UserRole.TEACHER, "CS/IT", "Faculty (Operating Systems)"),
            UserRosterItem("u4", "Rahul Verma", "rahul.v@igate.com", UserRole.STUDENT, "Mechanical", "Enrolled (GATE 2026)"),
            UserRosterItem("u5", "Chief Administrator", "admin@igate.com", UserRole.ADMIN, "All Branches", "Super Admin")
        )
    )
    val usersRoster: StateFlow<List<UserRosterItem>> = _usersRoster.asStateFlow()

    fun uploadLesson(title: String, subjectId: String, duration: String) {
        viewModelScope.launch {
            try {
                val newLesson = Lesson(
                    id = java.util.UUID.randomUUID().toString(),
                    subjectId = subjectId,
                    title = title,
                    description = "Master Curriculum",
                    videoUrl = "https://example.com/video.mp4",
                    durationString = duration,
                    educatorName = "Prof. Arvind Rao (IIT Bombay)"
                )
                repository.insertLesson(newLesson)
                _statusMessage.value = "Curriculum lesson '$title' published!"
            } catch (e: Exception) {
                _statusMessage.value = "Error publishing lesson: ${e.message}"
            }
        }
    }

    fun uploadNote(title: String, type: NoteType, subjectId: String) {
        viewModelScope.launch {
            try {
                val newNote = Note(
                    id = java.util.UUID.randomUUID().toString(),
                    subjectId = subjectId,
                    title = title,
                    description = "Official Institute Course Material",
                    type = type,
                    fileUrlOrContent = "mock_pdf_path.pdf",
                    isDownloaded = false
                )
                repository.insertNote(newNote)
                _statusMessage.value = "Document '$title' added to central library!"
            } catch (e: Exception) {
                _statusMessage.value = "Error adding document: ${e.message}"
            }
        }
    }

    fun changeUserRole(userId: String, newRole: UserRole) {
        _usersRoster.value = _usersRoster.value.map {
            if (it.id == userId) it.copy(role = newRole) else it
        }
        _statusMessage.value = "User role updated to ${newRole.name}!"
    }

    fun clearStatus() {
        _statusMessage.value = null
    }
}
