package com.example.gazelka.models
import kotlinx.serialization.Serializable

@Serializable
data class Chat(
    val chatId: Int,
    val otherUserId: Int?,
    val otherUserName: String?,
    val lastMessage: Message? = null,
    val orderId : Int? = null
)