package com.example.gazelka.pages

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.res.painterResource
import com.example.gazelka.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gazelka.AuthViewModel
import com.example.gazelka.models.Chat


@Composable
fun ChatsScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    var chats by remember { mutableStateOf<List<Chat>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val userRole = authViewModel.userData.collectAsState().value?.role

    LaunchedEffect(Unit) {
        authViewModel.getChats(
            onSuccess = { list ->
                chats = list
                isLoading = false
            },
            onError = { err ->
                error = err
                isLoading = false
                Toast.makeText(context, "Error: $err", Toast.LENGTH_LONG).show()
            }
        )
    }

    Box(
        modifier = modifier.fillMaxSize()
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

        Scaffold(
            bottomBar = {
                when (userRole) {
                    "customer" -> CustomerBottomNavigationBar(navController = navController, currentRoute = "customerChat")
                    "driver" -> DriverBottomNavigationBar(navController = navController, currentRoute = "driverChat")
                    else -> null
                }
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Failed to load chats: $error",
                            color = Color(0xFFFF6B6B),
                            fontSize = 16.sp
                        )
                    }
                }
                chats.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No chats yet",
                            fontSize = 16.sp,
                            color = Color(0xFF000000),
                            fontFamily = PoppinsFontFamily
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp, bottom = 16.dp)
                    ) {
                        items(chats) { chat ->
                            ChatCardItem(chat = chat, navController = navController)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatCardItem(chat: Chat, navController: NavController) {
    val statusColor = Color(0xFF22C55E)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("chat/${chat.orderId}")
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_user),
                    contentDescription = "Profile",
                    modifier = Modifier.size(39.dp),
                    tint = Color(0xFFFFFFFF)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${chat.otherUserName}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFFFFF),
                        fontFamily = PoppinsFontFamily
                    )

                    Spacer(Modifier.height(4.dp))

                    chat.lastMessage?.let {
                        Text(
                            text = "You: ${it.text}",
                            fontSize = 12.sp,
                            color = Color(0xFFCCCCCC),
                            fontFamily = PoppinsFontFamily,
                            maxLines = 1
                        )
                    } ?: run {
                        Text(
                            text = "No messages yet",
                            fontSize = 12.sp,
                            color = Color(0xFFCCCCCC),
                            fontFamily = PoppinsFontFamily
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = statusColor,
                            shape = CircleShape
                        )
                )

                Text(
                    text = "Order #${chat.orderId}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFFFFFFF),
                    fontFamily = PoppinsFontFamily
                )
            }
        }
    }
}
