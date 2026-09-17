package com.example.igate.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.igate.domain.model.GateBranch
import com.example.igate.domain.model.UserProfile
import com.example.igate.domain.model.UserRole
import com.example.igate.domain.repository.AuthRepository
import com.example.igate.domain.repository.IgateRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val repository: IgateRepository? = null
) : ViewModel() {

    val userProfile: StateFlow<UserProfile> = (repository?.currentUserProfile ?: kotlinx.coroutines.flow.flowOf(UserProfile()))
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfile()
        )

    fun switchRole(role: UserRole) {
        viewModelScope.launch {
            repository?.switchRole(role)
        }
    }

    fun updateBranch(branch: GateBranch) {
        viewModelScope.launch {
            val current = userProfile.value
            repository?.updateUserProfile(current.copy(branch = branch))
        }
    }

    fun logout() {
        authRepository.logout()
    }
}
