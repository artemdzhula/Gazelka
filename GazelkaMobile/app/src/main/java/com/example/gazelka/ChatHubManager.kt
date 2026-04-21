
package com.example.gazelka.signalr

import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import kotlinx.coroutines.flow.MutableSharedFlow
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.functions.Consumer
import com.microsoft.signalr.HubConnectionState

class ChatHubManager(
    private val baseUrl: String,
    private val tokenProvider: () -> String?
) {
    private var hubConnection: HubConnection? = null
    private var isConnecting = false

    val incomingMessages = MutableSharedFlow<MessageSignalR>(
        replay = 0,
        extraBufferCapacity = 64
    )

    fun connect() {
        if (hubConnection != null || isConnecting) return

        val token = tokenProvider()
        if (token.isNullOrBlank()) {
            println("❌ SignalR: JWT token is null")
            return
        }

        isConnecting = true

        hubConnection = HubConnectionBuilder
            .create("${baseUrl}chatHub")
            .withAccessTokenProvider(Single.just(token))
            .build()

        hubConnection?.on(
            "ReceiveMessage",
            { id: Int?, chatId: Int?, senderId: Int?, text: String?, sentAt: String? ->
                if (id != null && chatId != null && senderId != null && text != null && sentAt != null) {
                    incomingMessages.tryEmit(
                        MessageSignalR(id, chatId, senderId, text, sentAt)
                    )
                }
            },
            Int::class.java,
            Int::class.java,
            Int::class.java,
            String::class.java,
            String::class.java
        )

        hubConnection?.start()?.subscribe(
            {
                println("✅ SignalR connected")
                isConnecting = false
            },
            { error ->
                println("❌ SignalR error: ${error.message}")
                isConnecting = false
                hubConnection = null
            }
        )
    }

    fun disconnect() {
        hubConnection?.stop()?.subscribe()
        hubConnection = null
        isConnecting = false
    }
    fun enterChat(chatId: Int) {
        hubConnection?.invoke("EnterChat", chatId)
    }

    fun leaveChat() {
        hubConnection?.invoke("LeaveChat")
    }

}



data class MessageSignalR(
    val id: Int,
    val chatId: Int,
    val senderId: Int,
    val text: String,
    val sentAt: String,

)
