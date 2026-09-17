package com.example.igate.presentation.institutions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class Institution(
    val id: String,
    val name: String,
    val location: String,
    val rating: Double,
    val tags: List<String>,
    val imageUrl: String = "",
    val isFeatured: Boolean = false
)

class InstitutionsViewModel : ViewModel() {

    private val allInstitutions = listOf(
        Institution("1", "Aakash Institute", "New Delhi, India", 4.8, listOf("JEE", "NEET", "Medical"), isFeatured = true),
        Institution("2", "FIITJEE", "Hyderabad, India", 4.7, listOf("JEE Advanced", "Engineering"), isFeatured = true),
        Institution("3", "Allen Career Institute", "Kota, India", 4.9, listOf("NEET", "JEE Main", "Foundation"), isFeatured = true),
        Institution("4", "Vibrant Academy", "Kota, India", 4.5, listOf("JEE Advanced", "Physics"), isFeatured = false),
        Institution("5", "Resonance", "Jaipur, India", 4.4, listOf("JEE", "Pre-Medical", "Commerce"), isFeatured = false),
        Institution("6", "Bansal Classes", "Kota, India", 4.6, listOf("JEE", "Mathematics"), isFeatured = false),
        Institution("7", "Narayana Institute", "Bangalore, India", 4.3, listOf("JEE", "State Boards"), isFeatured = false)
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filteredInstitutions = MutableStateFlow(allInstitutions)
    val filteredInstitutions: StateFlow<List<Institution>> = _filteredInstitutions.asStateFlow()
    
    val featuredInstitutions: List<Institution> = allInstitutions.filter { it.isFeatured }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        filterInstitutions(query)
    }

    private fun filterInstitutions(query: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            if (query.isBlank()) {
                _filteredInstitutions.value = allInstitutions
            } else {
                val lowerQuery = query.lowercase().trim()
                _filteredInstitutions.value = allInstitutions.filter { inst ->
                    com.example.igate.domain.util.Algorithms.fuzzyMatch(lowerQuery, inst.name) ||
                    com.example.igate.domain.util.Algorithms.fuzzyMatch(lowerQuery, inst.location) ||
                    inst.tags.any { tag -> com.example.igate.domain.util.Algorithms.fuzzyMatch(lowerQuery, tag) }
                }
            }
        }
    }
}
