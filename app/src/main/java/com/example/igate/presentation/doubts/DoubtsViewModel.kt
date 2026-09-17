package com.example.igate.presentation.doubts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.igate.domain.model.Doubt
import com.example.igate.domain.repository.IgateRepository
import com.example.igate.data.analytics.AnalyticsTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DoubtsViewModel(
    private val repository: IgateRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    val doubts: StateFlow<List<Doubt>> = combine(
        repository.getDoubts(),
        _selectedFilter
    ) { allDoubts, filter ->
        if (filter == "All") allDoubts
        else if (filter == "Pending") allDoubts.filter { !it.isResolved }
        else if (filter == "Resolved") allDoubts.filter { it.isResolved }
        else allDoubts.filter { it.subjectName.contains(filter, ignoreCase = true) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun postDoubt(subject: String, title: String, detail: String, studentName: String = "Rachel Green") {
        viewModelScope.launch {
            try {
                repository.postDoubt(subject, title, detail, studentName)
                AnalyticsTracker.logDoubtPosted(subject)
            } catch (e: Exception) {
                // Safe catch
            }
        }
    }
}
