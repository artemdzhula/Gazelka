package com.example.gazelka

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import android.content.Intent
import com.example.gazelka.MainActivity



class AppFirebaseMessagingService : FirebaseMessagingService() {

    private val baseUrl = "http://10.0.2.2:5008/"
    private val channelId = "default_channel"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")

        applicationContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("fcm_token", token)
            .apply()

        sendTokenToBackend(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val type = message.data["type"]
        val orderId = message.data["orderId"]
        val chatId = message.data["chatId"]

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

            putExtra("push_type", type)
            putExtra("orderId", orderId)
            putExtra("chatId", chatId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = message.data["title"] ?: "Notification"
        val body = message.data["body"] ?: ""

        showNotification(title, body, pendingIntent)
    }




    private fun showNotification(title: String, body: String, pendingIntent: PendingIntent) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(
                    channelId,
                    "Default notifications",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Default channel for app notifications"
                    enableLights(true)
                    enableVibration(true)
                    lightColor = 0xFF00FF00.toInt()
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                }
                notificationManager.createNotificationChannel(channel)
            }
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(Random.nextInt(Int.MAX_VALUE), notification)
    }


    private fun sendTokenToBackend(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val jwtToken = TokenStorage.getAccessToken(applicationContext) ?: return@launch
                val json = Json.encodeToString(FcmTokenDto(token))
                val body = json.toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url("${baseUrl}api/push/token")
                    .addHeader("Authorization", "Bearer $jwtToken")
                    .post(body)
                    .build()

                OkHttpClient().newCall(request).execute().close()
                Log.d("FCM", "Token sent to backend")
            } catch (e: Exception) {
                Log.e("FCM", "Failed to send token", e)
            }
        }
    }
}

@Serializable
data class FcmTokenDto(val token: String)

object TokenStorage {
    fun getAccessToken(context: Context): String? {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        return prefs.getString("access_token", null)
    }
}
