package com.example.igate.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.igate.domain.model.GateBranch
import com.example.igate.domain.model.UserProfile
import com.example.igate.domain.model.UserRole
import com.example.igate.domain.repository.AuthRepository
import com.example.igate.domain.repository.IgateRepository
import com.example.igate.presentation.common.UiState
import com.example.igate.data.analytics.AnalyticsTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val igateRepository: IgateRepository? = null
) : ViewModel() {

    private val _authState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val authState: StateFlow<UiState<Unit>> = _authState.asStateFlow()

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = UiState.Loading
            val result = authRepository.login(email, pass)
            result.onSuccess {
                AnalyticsTracker.logLoginMethod("email")
                _authState.value = UiState.Success(Unit)
            }.onFailure {
                _authState.value = UiState.Error(it.message ?: "Authentication failed")
            }
        }
    }

    fun signup(
        email: String,
        pass: String,
        role: UserRole = UserRole.STUDENT,
        branch: GateBranch = GateBranch.CS,
        dob: String = "18 Feb 2002",
        name: String = "Rachel Green"
    ) {
        viewModelScope.launch {
            _authState.value = UiState.Loading
            val result = authRepository.signup(email, pass)
            result.onSuccess {
                try {
                    igateRepository?.updateUserProfile(
                        UserProfile(
                            id = "u_${System.currentTimeMillis()}",
                            name = name,
                            email = email,
                            role = role,
                            dob = dob,
                            branch = branch,
                            targetYear = "GATE 2027"
                        )
                    )
                    AnalyticsTracker.logSignUp("email")
                    AnalyticsTracker.setUserRole(role.name)
                    _authState.value = UiState.Success(Unit)
                } catch (e: Exception) {
                    _authState.value = UiState.Error(e.message ?: "Profile creation failed")
                }
            }.onFailure {
                _authState.value = UiState.Error(it.message ?: "Account creation failed")
            }
        }
    }


    private val _isOtpSent = MutableStateFlow(false)
    val isOtpSent: StateFlow<Boolean> = _isOtpSent.asStateFlow()

    private var currentVerificationId: String? = null

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = UiState.Loading
            val result = authRepository.signInWithGoogle(idToken)
            result.onSuccess {
                AnalyticsTracker.logLoginMethod("google")
                _authState.value = UiState.Success(Unit)
            }.onFailure {
                _authState.value = UiState.Error(it.message ?: "Google Sign-in failed")
            }
        }
    }

    fun sendPhoneOtp(phoneNumber: String, activity: android.app.Activity) {
        viewModelScope.launch {
            _authState.value = UiState.Loading
            val result = authRepository.sendPhoneOtp(phoneNumber, activity)
            result.onSuccess { verificationId ->
                currentVerificationId = verificationId
                _isOtpSent.value = true
                _authState.value = UiState.Idle
            }.onFailure {
                _authState.value = UiState.Error(it.message ?: "Failed to send OTP")
            }
        }
    }

    fun verifyPhoneOtp(code: String) {
        val verificationId = currentVerificationId ?: return
        viewModelScope.launch {
            _authState.value = UiState.Loading
            val result = authRepository.verifyPhoneOtp(verificationId, code)
            result.onSuccess {
                AnalyticsTracker.logLoginMethod("phone")
                _authState.value = UiState.Success(Unit)
            }.onFailure {
                _authState.value = UiState.Error(it.message ?: "OTP Verification failed")
            }
        }
    }

    fun resetState() {
        _authState.value = UiState.Idle
        _isOtpSent.value = false
        currentVerificationId = null
    }
}
