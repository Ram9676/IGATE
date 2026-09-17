package com.example.igate.presentation.teacher

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

class TeacherViewModel(
    private val repository: IgateRepository
) : ViewModel() {

    val batches: StateFlow<List<Batch>> = repository.getBatches()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val pendingDoubts: StateFlow<List<Doubt>> = repository.getDoubts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun uploadLecture(title: String, subjectId: String, duration: String, videoUrl: String) {
        viewModelScope.launch {
            val newLesson = Lesson(
                id = "l_${System.currentTimeMillis()}",
                subjectId = subjectId,
                title = title,
                description = "Uploaded by Faculty",
                videoUrl = videoUrl,
                durationString = duration,
                educatorName = "Prof. Arvind Rao (IIT Bombay)"
            )
            repository.insertLesson(newLesson)
            _statusMessage.value = "Lecture '$title' published successfully!"
        }
    }

    fun uploadPdfNote(title: String, subjectId: String, fileUrl: String) {
        viewModelScope.launch {
            val newNote = Note(
                id = "n_${System.currentTimeMillis()}",
                subjectId = subjectId,
                title = title,
                description = "High-Yield Faculty Handout",
                type = NoteType.PDF,
                fileUrlOrContent = fileUrl,
                isDownloaded = false
            )
            repository.insertNote(newNote)
            _statusMessage.value = "PDF '$title' added to student library!"
        }
    }

    fun answerDoubt(doubtId: String, answer: String, teacherName: String = "Prof. Arvind Rao (IIT Bombay)") {
        viewModelScope.launch {
            repository.answerDoubt(doubtId, answer, teacherName)
            _statusMessage.value = "Doubt resolved and sent to student!"
        }
    }

    fun createBatch(name: String, branch: GateBranch, schedule: String) {
        viewModelScope.launch {
            val newBatch = Batch(
                id = "b_${System.currentTimeMillis()}",
                name = name,
                branch = branch,
                educatorName = "Prof. Arvind Rao",
                studentCount = 0,
                scheduleTime = schedule,
                syllabusProgressPercentage = 0
            )
            repository.createBatch(newBatch)
            _statusMessage.value = "Batch '$name' created!"
        }
    }

    fun clearStatus() {
        _statusMessage.value = null
    }
}
