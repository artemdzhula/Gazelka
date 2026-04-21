package com.example.gazelka.models

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Message(
    val id: Int? = null,
    var chatId: Int? = null,
    val senderId: Int,
    val text: String,
    val sentAt: String // ISO UTC: "2025-12-09T20:45:00Z"
)