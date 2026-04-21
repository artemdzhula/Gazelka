package com.example.gazelka

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gazelka.signalr.ChatHubManager

class AuthViewModelFactory(
    private val application: Application,
    private val baseUrl: String = "http://10.0.2.2:5008/",
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(application, baseUrl) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
