package com.example.igate.presentation.lessons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.igate.domain.model.*
import com.example.igate.domain.repository.IgateRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LessonsViewModel(
    private val repository: IgateRepository
) : ViewModel() {

    private val _selectedBranch = MutableStateFlow(GateBranch.CS)
    val selectedBranch: StateFlow<GateBranch> = _selectedBranch.asStateFlow()

    private val _selectedSubject = MutableStateFlow<Subject?>(null)
    val selectedSubject: StateFlow<Subject?> = _selectedSubject.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    val userProfile: StateFlow<UserProfile> = repository.currentUserProfile
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserProfile())

    init {
        viewModelScope.launch {
            repository.currentUserProfile.collect { profile ->
                _selectedBranch.value = profile.branch
            }
        }
    }

    val subjects: StateFlow<List<Subject>> = _selectedBranch.flatMapLatest { branch ->
        repository.getSubjectsForBranch(branch)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lessons: StateFlow<List<Lesson>> = _selectedSubject.flatMapLatest { subject ->
        if (subject == null) {
            repository.getRecentLessons()
        } else {
            repository.getLessonsForSubject(subject.id)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<Note>> = _selectedSubject.flatMapLatest { subject ->
        if (subject == null) {
            repository.getPinnedOrDownloadedNotes()
        } else {
            repository.getNotesForSubject(subject.id)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectBranch(branch: GateBranch) {
        _selectedBranch.value = branch
        _selectedSubject.value = null
    }

    fun selectSubject(subject: Subject?) {
        _selectedSubject.value = subject
    }

    fun clearStatus() {
        _statusMessage.value = null
    }

    fun uploadLesson(
        title: String,
        subjectId: String,
        topicName: String,
        duration: String,
        videoUrl: String,
        pdfTitle: String,
        timestamps: List<VideoTimestamp>
    ) {
        viewModelScope.launch {
            val lessonId = "l_${System.currentTimeMillis()}"
            val newLesson = Lesson(
                id = lessonId,
                subjectId = subjectId,
                topicId = "t_${System.currentTimeMillis()}",
                topicName = topicName.ifBlank { "Core Concepts" },
                title = title,
                description = "Uploaded via Direct Upload Portal",
                videoUrl = videoUrl.ifBlank { "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4" },
                durationString = duration.ifBlank { "45 mins" },
                educatorName = userProfile.value.name,
                isCompleted = false,
                pdfAttachmentTitle = pdfTitle.ifBlank { "$title Notes.pdf" },
                pdfAssetFileName = "mock_pdf_path.pdf",
                timestamps = timestamps
            )
            repository.insertLesson(newLesson)
            _statusMessage.value = "Lesson \"$title\" published successfully!"
        }
    }
}
