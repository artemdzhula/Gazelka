package com.example.gazelka.pages

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import com.example.gazelka.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gazelka.AuthViewModel
import com.example.gazelka.models.Message
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import com.example.gazelka.models.Order
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.unit.Dp
import com.example.gazelka.models.Chat
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    orderId: Int
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val messages by authViewModel.chatMessages.collectAsState()
    val currentChatId by authViewModel.currentChatId.collectAsState()
    val currentUserId = authViewModel.userData.collectAsState().value?.id ?: 0
    var newMessage by remember { mutableStateOf("") }
    val role = authViewModel.userData.collectAsState().value?.role ?: "customer"

    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val keyboardHeight: State<Dp> = rememberKeyboardHeight()

    LaunchedEffect(orderId) {
        authViewModel.loadChat(orderId) { err ->
            if (err != null) {
                scope.launch(Dispatchers.Main) {
                    Toast.makeText(context, "Error: $err", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(currentChatId) {
        currentChatId?.let { chatId ->
            authViewModel.loadChatHistory(chatId)
            authViewModel.enterChat(chatId)
        }
    }

    LaunchedEffect(Unit) {
        authViewModel.connectToChatHub()
    }

    DisposableEffect(Unit) {
        onDispose {
            authViewModel.leaveChat()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Image(
            painter = painterResource(id = R.drawable.wallpaper),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF000000).copy(alpha = 0.15f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (role == "driver") {
                            navController.navigate("DriverOrderDetails/$orderId")
                        } else {
                            navController.navigate("CustomerOrderDetails/$orderId")
                        }
                    }
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.size(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9C80E)),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back",
                            tint = Color(0xFF1A1A1A),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = Color(0xFF22C55E),
                                        shape = CircleShape
                                    )
                            )

                            Text(
                                text = "Order #${orderId}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFFFFFF)
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = "Chat with ",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFFFFF)
                        )
                    }

                    Spacer(Modifier.width(50.dp))
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(messages) { msg ->
                    val isSender = msg.senderId == currentUserId
                    Row(
                        horizontalArrangement = if (isSender) Arrangement.End else Arrangement.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isSender) Color(0xFF2A2A2A) else Color(0xFFE7C01A),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = msg.text,
                                color = if (isSender) Color.White else Color(0xFF1A1A1A),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp, bottom = 20.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFF2A2A2A),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextField(
                        value = newMessage,
                        onValueChange = { newMessage = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                "Type a message",
                                fontSize = 14.sp,
                                color = Color(0xFFCCCCCC)
                                // fontFamily = PoppinsFontFamily
                            )
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 14.sp,
                            color = Color(0xFFFFFFFF)
                            // fontFamily = PoppinsFontFamily
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color(0xFFF9C80E)
                        ),
                        minLines = 1,
                        maxLines = 5
                    )

                    Button(
                        onClick = {
                            if (newMessage.isNotBlank()) {
                                scope.launch {
                                    authViewModel.sendMessage(newMessage, orderId) { err ->
                                        if (err != null) {
                                            scope.launch(Dispatchers.Main) {
                                                Toast.makeText(
                                                    context,
                                                    "Error: $err",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                    newMessage = ""
                                }
                            }
                        },
                        modifier = Modifier
                            .width(72.dp)
                            .height(38.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9C80E)),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_send),
                            contentDescription = "Send",
                            tint = Color(0xFF1A1A1A),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun rememberKeyboardHeight(): androidx.compose.runtime.State<Dp> {
    val density = LocalDensity.current
    val keyboardHeight = remember { mutableStateOf(0.dp) }

    DisposableEffect(Unit) {
        onDispose { }
    }

    return keyboardHeight
}
