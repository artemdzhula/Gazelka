package com.example.gazelka.pages

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gazelka.AuthViewModel
import com.example.gazelka.R
import kotlinx.coroutines.launch
import com.example.gazelka.RegistrationViewModel

@Composable
fun RegPhoneInfoScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    regViewModel: RegistrationViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var phone by rememberSaveable { mutableStateOf("") }
    var phoneError by remember { mutableStateOf(false) }

    val buttonText = when (regViewModel.role) {
        "customer" -> "Create user profile"
        "driver" -> "Next step"
        else -> "Next step"
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

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
                        text = "Registration",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF9C80E),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            phoneError = false
                        },
                        placeholder = { Text("Phone number", color = Color(0xFF999999)) },
                        isError = phoneError,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFE8E8E8),
                            focusedContainerColor = Color(0xFFE8E8E8),
                            errorContainerColor = Color(0xFFE8E8E8),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            errorBorderColor = Color(0xFFE91E63),
                            unfocusedTextColor = Color(0xFF000000),
                            focusedTextColor = Color(0xFF000000),
                            errorTextColor = Color(0xFFE91E63)
                        )
                    )

                    if (phoneError) {
                        Text(
                            text = "Enter valid phone number (+1234567890)",
                            color = Color(0xFFE91E63),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            phoneError = false

                            val phoneRegex = Regex("^\\+?\\d{10,15}$")
                            if (phone.isBlank() || !phoneRegex.matches(phone.trim())) {
                                phoneError = true
                            } else {
                                regViewModel.phone = phone.trim()

                                if (regViewModel.role == "customer") {
                                    coroutineScope.launch {
                                        authViewModel.register(
                                            email = regViewModel.email,
                                            password = regViewModel.password,
                                            name = regViewModel.name,
                                            surname = regViewModel.surname,
                                            phone = regViewModel.phone,
                                            role = "customer",
                                            carType = null,
                                            carColor = null,
                                            carNumber = null,
                                            cityName = null,
                                            onSuccess = {
                                                navController.navigate("emailConfirmation/${regViewModel.email}") {
                                                    popUpTo("welcome") { inclusive = true }
                                                    launchSingleTop = true
                                                }
                                            },
                                            onError = { err ->
                                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                                regViewModel.role = "customer"
                                                navController.navigate("basicInfoReg")
                                            }
                                        )
                                    }
                                } else {
                                    navController.navigate("carInfoReg")
                                }
                            }
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
                            text = buttonText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF000000),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
