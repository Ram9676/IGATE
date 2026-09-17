package com.example.igate.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.igate.domain.model.Note
import com.example.igate.domain.repository.IgateRepository
import com.example.igate.presentation.common.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

class LibraryViewModel(
    repository: IgateRepository
) : ViewModel() {

    val allNotes: StateFlow<UiState<List<Note>>> = repository.getNotesForSubject("s1")
        .onStart { delay(1000) }
        .map { UiState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )
        
    private val _savedDocumentIds = MutableStateFlow<Set<String>>(emptySet())
    val savedDocumentIds: StateFlow<Set<String>> = _savedDocumentIds.asStateFlow()
    
    fun toggleBookmark(documentId: String) {
        val current = _savedDocumentIds.value
        if (current.contains(documentId)) {
            _savedDocumentIds.value = current - documentId
        } else {
            _savedDocumentIds.value = current + documentId
        }
    }
}
