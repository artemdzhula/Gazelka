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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gazelka.AuthViewModel
import com.example.gazelka.R
import com.example.gazelka.RegistrationViewModel

@Composable
fun PasswordRecoveryScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current

    var code by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var codeError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val email = authViewModel._userData.value?.email ?: ""

    LaunchedEffect(email) {
        authViewModel.sendPasswordReset(
            email = email,
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
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 40.dp)
                    .padding(bottom = 14.dp)
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
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9C80E)),
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
                            text = "Password Recovery",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = PoppinsFontFamily
                        )

                        Spacer(modifier = Modifier.size(40.dp))
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = code,
                                onValueChange = {
                                    code = it
                                    codeError = false
                                },
                                placeholder = { Text("Code from email", color = Color(0xFF999999)) },
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

                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = {
                                    newPassword = it
                                    passwordError = false
                                },
                                placeholder = { Text("New password", color = Color(0xFF999999)) },
                                isError = passwordError,
                                singleLine = true,
                                visualTransformation = if (passwordVisible) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color(0xFFE8E8E8),
                                    focusedContainerColor = Color(0xFFE8E8E8),
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent
                                ),
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            painter = painterResource(
                                                id = if (passwordVisible)
                                                    R.drawable.ic_visibility_off
                                                else
                                                    R.drawable.ic_visibility_on
                                            ),
                                            contentDescription = if (passwordVisible)
                                                "Hide password"
                                            else
                                                "Show password",
                                            tint = Color(0xFF1E1F22),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            )


                            Button(
                                onClick = {
                                    authViewModel.changePassword(
                                        email = email,
                                        code = code,
                                        newPassword = newPassword,
                                        onSuccess = {
                                            Toast.makeText(
                                                context,
                                                "Password changed",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            navController.navigate("login") {
                                                popUpTo(0)
                                            }
                                        },
                                        onError = {
                                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
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
                            ) {
                                Text(
                                    "Save Changes",
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
                                    authViewModel.sendPasswordReset(
                                        email = email,
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
                        }
                    }
                }
            }
        }
    }
}
