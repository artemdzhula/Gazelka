package com.example.gazelka

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.example.gazelka.ui.theme.GazelkaTheme
import com.example.gazelka.GazelkaNavigation
import com.example.gazelka.AuthViewModelFactory
import com.example.gazelka.AuthViewModel
import com.example.gazelka.RegistrationViewModel
import com.google.android.libraries.places.api.Places

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        FirebaseApp.initializeApp(this)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "APIKEY")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val factory = AuthViewModelFactory(application)
        val authViewModel: AuthViewModel by viewModels { factory }
        val regViewModel: RegistrationViewModel by viewModels()

        setContent {
            GazelkaTheme {
                GazelkaNavigation(
                    modifier = Modifier.padding(),
                    authViewModel = authViewModel,
                    regViewModel = regViewModel,
                    startIntent = intent
                )
            }
        }

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                Log.d("FCM", token)
            }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
