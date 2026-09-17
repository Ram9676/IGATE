package com.example.igate.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isAuthenticated: Flow<Boolean>
    val currentUserEmail: Flow<String?>
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun signup(email: String, password: String): Result<Unit>
    fun logout()
    fun forceDemoLogin()
    
    // Google Auth
    suspend fun signInWithGoogle(idToken: String): Result<Unit>
    
    // Phone Auth
    suspend fun sendPhoneOtp(phoneNumber: String, activity: android.app.Activity): Result<String>
    suspend fun verifyPhoneOtp(verificationId: String, code: String): Result<Unit>
}
