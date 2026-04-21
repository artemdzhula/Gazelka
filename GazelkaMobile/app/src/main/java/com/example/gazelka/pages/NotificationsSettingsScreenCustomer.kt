package com.example.gazelka.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gazelka.AuthViewModel
import com.example.gazelka.R
import com.example.gazelka.models.NotificationSettings

@Composable
fun NotificationsSettingsScreenCustomer(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val scrollState = rememberScrollState()

    val settings by authViewModel.notificationSettings.collectAsState()


    if (settings == null) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFFF9C80E))
        }
        return
    }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val notificationTimeOptions = listOf(
        "0 min" to 0,
        "5 min" to 5,
        "10 min" to 10,
        "15 min" to 15,
        "30 min" to 30,
        "1 hour" to 60
    )

    LaunchedEffect(Unit) {
        authViewModel.loadNotificationSettingsFromServer()
    }

    Box(modifier = modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.wallpaper),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.15f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            /* ---------- TOP BAR ---------- */

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.size(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9C80E)),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }

                    Text(
                        text = "Notifications",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = PoppinsFontFamily
                    )

                    Spacer(Modifier.size(40.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            /* ---------- SETTINGS CARD ---------- */

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    SettingSwitch(
                        title = "Notify me about chat notifications",
                        checked = settings!!.chatEnabled
                    ) {
                        authViewModel.updateNotificationSettings(
                            settings!!.copy(chatEnabled = it)
                        )
                    }

                    SettingSwitch(
                        title = "Send me if order status changed",
                        checked = settings!!.statusEnabled
                    ) {
                        authViewModel.updateNotificationSettings(
                            settings!!.copy(statusEnabled = it)
                        )
                    }

                    /* ---------- UPCOMING ORDER ---------- */

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Notify me about upcoming orders",
                            fontSize = 14.sp,
                            color = Color.White,
                            fontFamily = PoppinsFontFamily,
                            modifier = Modifier.weight(1f)
                        )

                        Box {
                            TextButton(
                                onClick = { dropdownExpanded = true },
                                colors = ButtonDefaults.textButtonColors(
                                    containerColor = Color(0xFFE8E8E8),
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("${settings!!.upcomingMinutes} min")
                            }

                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                notificationTimeOptions.forEach { (label, minutes) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            var upcoming = true
                                            if(minutes == 0) upcoming = false
                                            authViewModel.updateNotificationSettings(
                                                settings!!.copy(
                                                    upcomingEnabled = upcoming,
                                                    upcomingMinutes = minutes
                                                )
                                            )
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ---------- REUSABLE SWITCH ---------- */

@Composable
private fun SettingSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            color = Color.White,
            fontFamily = PoppinsFontFamily
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFFF9C80E)
            )
        )
    }
}
