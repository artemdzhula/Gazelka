package com.example.gazelka.pages

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.gazelka.RegistrationViewModel

@Composable
fun EmailConfirmationScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    email: String?,
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var code by rememberSaveable { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var codeError by remember { mutableStateOf(false) }


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
                .background(Color(0xFF000000).copy(alpha = 0.15f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1A1A1A)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Confirm your email",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF9C80E),
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        OutlinedTextField(
                            value = code,
                            onValueChange = {
                                code = it
                                codeError = false
                            },
                            placeholder = { Text("Enter code from mail", color = Color(0xFF999999)) },
                            isError = codeError,
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color(0xFFE8E8E8),
                                focusedContainerColor = Color(0xFFE8E8E8),
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent
                            )
                        )

                        Button(
                            onClick = {
                                if (code.isBlank()) {
                                    codeError = true
                                    return@Button
                                }

                                authViewModel.confirmEmail(
                                    code = code,
                                    email = email!!,
                                    onSuccess = {
                                        Toast
                                            .makeText(
                                                context,
                                                "Email confirmed",
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()

                                        navController.navigate("login") {
                                            popUpTo(0)
                                        }
                                    },
                                    onError = {
                                        codeError = true
                                        Toast
                                            .makeText(
                                                context,
                                                it,
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF9C80E)
                            )
                        ){
                            Text(
                                "Create user profile",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                            )
                        }


                        Text(
                            text = "Didn't get email? Resend it",
                            fontSize = 14.sp,
                            color = Color(0xFFF9C80E),
                            modifier = Modifier.clickable {
                                authViewModel.resendEmail(
                                    email = email!!,
                                    onSuccess = {
                                        Toast
                                            .makeText(
                                                context,
                                                "Code sent",
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()
                                    },
                                    onError = {
                                        Toast
                                            .makeText(
                                                context,
                                                it,
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()
                                    }
                                )
                            }
                        )
                        Text(
                            text = "Dont want to confirm email",
                            fontSize = 14.sp,
                            color = Color(0xFFF9C80E),
                            modifier = Modifier.clickable {
                                navController.navigate("login"){
                                    popUpTo(0)
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
