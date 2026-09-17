package com.example.igate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.igate.presentation.main.MainScreen
import com.example.igate.theme.IGATETheme

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.igate.presentation.auth.LoginScreen

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val authRepository = (application as IgateApplication).container.authRepository

    enableEdgeToEdge()
    androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
    setContent {
      IGATETheme { 
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { 
            val isAuthenticated by authRepository.isAuthenticated.collectAsState(initial = false)
            if (isAuthenticated) {
                MainScreen() 
            } else {
                LoginScreen()
            }
        } 
      }
    }
  }
}
