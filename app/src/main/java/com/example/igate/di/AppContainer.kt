package com.example.igate.di

import android.content.Context
import com.example.igate.data.repository.FirestoreRepositoryImpl
import com.example.igate.data.chat.FirebaseChatRepository
import com.example.igate.domain.repository.IgateRepository
import com.example.igate.domain.repository.AuthRepository
import com.example.igate.data.repository.AuthRepositoryImpl

interface AppContainer {
    val igateRepository: IgateRepository
    val authRepository: AuthRepository
    val chatRepository: FirebaseChatRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val igateRepository: IgateRepository by lazy {
        FirestoreRepositoryImpl()
    }

    override val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl()
    }

    override val chatRepository: FirebaseChatRepository by lazy {
        FirebaseChatRepository()
    }
}
