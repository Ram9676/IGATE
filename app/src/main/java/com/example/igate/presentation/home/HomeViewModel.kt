package com.example.igate.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.igate.domain.model.*
import com.example.igate.domain.repository.IgateRepository
import com.example.igate.presentation.common.UiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: IgateRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val userProfile: StateFlow<UserProfile> = repository.currentUserProfile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfile()
        )

    val subjects: StateFlow<UiState<List<Subject>>> = combine(
        repository.getAllSubjects(),
        _searchQuery
    ) { allSubjects, query ->
        val filtered = if (query.isBlank()) {
            allSubjects
        } else {
            allSubjects.filter { 
                it.name.contains(query, ignoreCase = true) || 
                it.description.contains(query, ignoreCase = true) 
            }
        }
        UiState.Success(filtered)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Loading
    )

    val recommendedLessons: StateFlow<UiState<List<Lesson>>> = repository.getRecentLessons()
        .map { UiState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    val availableTests: StateFlow<List<GateTest>> = repository.getAvailableTests()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentResults: StateFlow<List<TestAttemptResult>> = repository.getRecentTestResults()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            try {
                repository.seedMockDataIfEmpty()
            } catch (e: Exception) {
                // Ignore seed errors so app doesn't crash on startup
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun switchBranch(branch: GateBranch) {
        viewModelScope.launch {
            try {
                val current = userProfile.value
                repository.updateUserProfile(current.copy(branch = branch))
            } catch (e: Exception) {
                // Handle offline or error gracefully
            }
        }
    }
}
