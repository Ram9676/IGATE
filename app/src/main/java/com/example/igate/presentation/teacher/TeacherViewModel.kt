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

    fun uploadLecture(
        title: String,
        subjectId: String,
        duration: String,
        videoUrl: String,
        topicName: String = "Core Topics",
        pdfTitle: String = "",
        timestamps: List<VideoTimestamp> = emptyList()
    ) {
        viewModelScope.launch {
            try {
                val newLesson = Lesson(
                    id = "l_${System.currentTimeMillis()}",
                    subjectId = subjectId,
                    topicId = "t_${System.currentTimeMillis()}",
                    topicName = topicName.ifBlank { "Core Topics" },
                    title = title,
                    description = "Uploaded by Faculty with Video Timers",
                    videoUrl = videoUrl.ifBlank { "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4" },
                    durationString = duration.ifBlank { "45 mins" },
                    educatorName = "Prof. Arvind Rao (IIT Bombay)",
                    pdfAttachmentTitle = pdfTitle.ifBlank { "$title Notes.pdf" },
                    pdfAssetFileName = "mock_pdf_path.pdf",
                    timestamps = timestamps
                )
                repository.insertLesson(newLesson)
                _statusMessage.value = "Lecture '$title' with ${timestamps.size} chapters published!"
            } catch (e: Exception) {
                _statusMessage.value = "Error publishing lecture: ${e.message}"
            }
        }
    }

    fun uploadPdfNote(title: String, subjectId: String, fileUrl: String) {
        viewModelScope.launch {
            try {
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
            } catch (e: Exception) {
                _statusMessage.value = "Error uploading PDF: ${e.message}"
            }
        }
    }

    fun answerDoubt(doubtId: String, answer: String, teacherName: String = "Prof. Arvind Rao (IIT Bombay)") {
        viewModelScope.launch {
            try {
                repository.answerDoubt(doubtId, answer, teacherName)
                _statusMessage.value = "Doubt resolved and sent to student!"
            } catch (e: Exception) {
                _statusMessage.value = "Error answering doubt: ${e.message}"
            }
        }
    }

    fun createBatch(name: String, branch: GateBranch, schedule: String) {
        viewModelScope.launch {
            try {
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
            } catch (e: Exception) {
                _statusMessage.value = "Error creating batch: ${e.message}"
            }
        }
    }

    fun clearStatus() {
        _statusMessage.value = null
    }
}
