package com.example.igate.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.igate.domain.repository.AuthRepository
import com.example.igate.domain.repository.IgateRepository
import com.example.igate.presentation.home.HomeViewModel
import com.example.igate.presentation.library.LibraryViewModel
import com.example.igate.presentation.lessons.LessonsViewModel
import com.example.igate.presentation.institutions.InstitutionsViewModel
import com.example.igate.presentation.auth.LoginViewModel
import com.example.igate.presentation.doubts.DoubtsViewModel
import com.example.igate.presentation.teacher.TeacherViewModel

class IgateViewModelFactory(
    private val repository: IgateRepository,
    private val authRepository: AuthRepository? = null
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(repository) as T
            }
            modelClass.isAssignableFrom(LibraryViewModel::class.java) -> {
                LibraryViewModel(repository) as T
            }
            modelClass.isAssignableFrom(LessonsViewModel::class.java) -> {
                LessonsViewModel(repository) as T
            }
            modelClass.isAssignableFrom(InstitutionsViewModel::class.java) -> {
                InstitutionsViewModel() as T
            }
            modelClass.isAssignableFrom(com.example.igate.presentation.admin.AdminViewModel::class.java) -> {
                com.example.igate.presentation.admin.AdminViewModel(repository) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                if (authRepository == null) throw IllegalArgumentException("AuthRepository is required for LoginViewModel")
                LoginViewModel(authRepository, repository) as T
            }
            modelClass.isAssignableFrom(com.example.igate.presentation.profile.ProfileViewModel::class.java) -> {
                if (authRepository == null) throw IllegalArgumentException("AuthRepository is required for ProfileViewModel")
                com.example.igate.presentation.profile.ProfileViewModel(authRepository, repository) as T
            }
            modelClass.isAssignableFrom(com.example.igate.presentation.quiz.QuizViewModel::class.java) -> {
                com.example.igate.presentation.quiz.QuizViewModel(repository) as T
            }
            modelClass.isAssignableFrom(DoubtsViewModel::class.java) -> {
                DoubtsViewModel(repository) as T
            }
            modelClass.isAssignableFrom(TeacherViewModel::class.java) -> {
                TeacherViewModel(repository) as T
            }
            modelClass.isAssignableFrom(com.example.igate.presentation.chat.ChatViewModel::class.java) -> {
                com.example.igate.presentation.chat.ChatViewModel() as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class ${modelClass.name}")
        }
    }
}
