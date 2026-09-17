package com.example.igate.presentation.lessons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.igate.domain.model.Subject
import com.example.igate.domain.repository.IgateRepository
import com.example.igate.presentation.common.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class LessonsViewModel(
    repository: IgateRepository
) : ViewModel() {

    val subjects: StateFlow<UiState<List<Subject>>> = repository.getAllSubjects()
        .onStart { delay(1000) }
        .map { UiState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )
}
