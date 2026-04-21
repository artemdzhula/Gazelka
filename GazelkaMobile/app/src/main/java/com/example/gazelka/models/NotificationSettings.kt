package com.example.gazelka.models
import kotlinx.serialization.Serializable
@Serializable
data class NotificationSettings(
    val chatEnabled: Boolean = true,
    val newOrdersEnabled: Boolean = true,
    val statusEnabled: Boolean = true,
    val upcomingEnabled: Boolean = true,
    val upcomingMinutes: Int = 15
)
