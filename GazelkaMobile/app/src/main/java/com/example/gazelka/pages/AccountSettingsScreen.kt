package com.example.gazelka.pages

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gazelka.AuthViewModel
import com.example.gazelka.R

@Composable
fun AccountSettingsScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val user by authViewModel.userData.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }

    val userName = user?.name ?: "-"
    val userSurname = user?.surname ?: "-"
    val userEmail = user?.email ?: "-"
    val userPhone = user?.phoneNumber ?: "-"
    val roleValue = user?.role ?: "-"

    val carTypeValue = user?.carType ?: "-"
    val carColorValue = user?.carColor ?: "-"
    val carNumberValue = user?.carNumber ?: "-"

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
            containerColor = Color.Transparent,
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 52.dp
                        )
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AccountActionButton(
                                text = "Change Password",
                                icon = R.drawable.ic_chpassword,
                                containerColor = Color(0xFFF9C80E),
                                textColor = Color(0xFF000000)
                            ) {
                                navController.navigate("passwordRecovery")
                            }

                            AccountActionButton(
                                text = "Delete Account",
                                icon = R.drawable.ic_delete,
                                containerColor = Color(0xFFD32F2F),
                                textColor = Color.White
                            ) {
                                showDeleteDialog = true
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .padding(top = 24.dp)
            ) {
                val scrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier.size(40.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF9C80E)
                                ),
                                shape = CircleShape,
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_arrow_back),
                                    contentDescription = "Back",
                                    tint = Color(0xFF1A1A1A),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Text(
                                text = "Account",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = PoppinsFontFamily
                            )

                            Spacer(modifier = Modifier.size(40.dp))
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Text(
                                text = "Profile info",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                fontFamily = PoppinsFontFamily
                            )

                            Spacer(Modifier.height(12.dp))

                            AccountInfoRow(
                                icon = R.drawable.ic_user,
                                value = "$userName $userSurname"
                            )
                            AccountInfoRow(
                                icon = R.drawable.ic_role,
                                value = roleValue
                            )
                            AccountInfoRow(
                                icon = R.drawable.ic_email,
                                value = userEmail
                            )
                            AccountInfoRow(
                                icon = R.drawable.ic_phone,
                                value = userPhone
                            )

                            Spacer(Modifier.height(8.dp))

                            AccountActionButton(
                                text = "Edit",
                                icon = R.drawable.ic_edit,
                                containerColor = Color(0xFFF9C80E),
                                textColor = Color(0xFF000000)
                            ) {
                                navController.navigate("editProfile")
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    if (roleValue.lowercase() == "driver") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                Text(
                                    text = "Car info",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    fontFamily = PoppinsFontFamily
                                )

                                Spacer(Modifier.height(12.dp))

                                AccountInfoRow(
                                    icon = R.drawable.ic_plate,
                                    value = carNumberValue
                                )
                                AccountInfoRow(
                                    icon = R.drawable.ic_van,
                                    value = carTypeValue
                                )
                                AccountInfoRow(
                                    icon = R.drawable.ic_color,
                                    value = carColorValue
                                )

                                Spacer(Modifier.height(8.dp))

                                AccountActionButton(
                                    text = "Edit",
                                    icon = R.drawable.ic_edit,
                                    containerColor = Color(0xFFF9C80E),
                                    textColor = Color(0xFF000000)
                                ) {
                                    navController.navigate("editCar")
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                    } else {
                        Spacer(Modifier.height(24.dp))
                    }

                    Spacer(Modifier.height(80.dp))
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Account") },
            text = { Text("Are you sure you want to delete your account? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        authViewModel.deleteAccount(
                            onSuccess = {
                                Toast.makeText(context, "Account deleted", Toast.LENGTH_SHORT)
                                    .show()
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onError = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                ) {
                    Text("Delete", color = Color(0xFFD32F2F))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AccountInfoRow(
    icon: Int,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = Color(0xFFFFFFFF),
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.White,
            fontFamily = PoppinsFontFamily
        )
    }
}

@Composable
private fun AccountActionButton(
    text: String,
    icon: Int,
    containerColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = text,
                tint = textColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                fontFamily = PoppinsFontFamily
            )
        }
    }
}
