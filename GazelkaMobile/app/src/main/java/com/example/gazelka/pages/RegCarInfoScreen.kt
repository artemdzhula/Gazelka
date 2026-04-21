package com.example.gazelka.pages

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
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
fun RegCarInfoScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    regViewModel: RegistrationViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var carModel by rememberSaveable { mutableStateOf("") }
    var carType by rememberSaveable { mutableStateOf("") }
    var carColor by rememberSaveable { mutableStateOf("") }
    var carNumber by rememberSaveable { mutableStateOf("") }

    var carModelError by remember { mutableStateOf(false) }
    var carTypeError by remember { mutableStateOf(false) }
    var carColorError by remember { mutableStateOf(false) }
    var carNumberError by remember { mutableStateOf(false) }

    val carTypeOptions = listOf(
        "Small Van — 3 m³ / up to 600 kg" to "Small Van",
        "Medium Van — 6 m³ / up to 1000 kg" to "Medium Van",
        "Large Van — 12 m³ / up to 1500 kg" to "Large Van",
        "Luton Van — 20 m³ / up to 3500 kg" to "Luton Van"
    )
    val carColors = listOf("Red", "Green", "Blue", "White", "Black")

    var expandedType by remember { mutableStateOf(false) }
    var expandedColor by remember { mutableStateOf(false) }

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

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .clickable { expandedType = true },
                        colors = CardDefaults.cardColors(
                            containerColor = if (carTypeError) Color(0xFFE8E8E8) else Color(0xFFE8E8E8)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = if (carType.isNotEmpty()) carType else "Car Type",
                                color = if (carType.isNotEmpty()) Color(0xFF000000) else Color(0xFF999999),
                                fontSize = 16.sp
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = expandedType,
                        onDismissRequest = { expandedType = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        carTypeOptions.forEach { (label, value) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    carType = value
                                    expandedType = false
                                    carTypeError = false
                                }
                            )
                        }
                    }
                    if (carTypeError) {
                        Text(
                            text = "Select car type",
                            color = Color(0xFFE91E63),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .clickable { expandedColor = true },
                        colors = CardDefaults.cardColors(
                            containerColor = if (carColorError) Color(0xFFE8E8E8) else Color(0xFFE8E8E8)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = if (carColor.isNotEmpty()) carColor else "Car Color",
                                color = if (carColor.isNotEmpty()) Color(0xFF000000) else Color(0xFF999999),
                                fontSize = 16.sp
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = expandedColor,
                        onDismissRequest = { expandedColor = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        carColors.forEach { color ->
                            DropdownMenuItem(
                                text = { Text(color) },
                                onClick = {
                                    carColor = color
                                    expandedColor = false
                                    carColorError = false
                                }
                            )
                        }
                    }
                    if (carColorError) {
                        Text(
                            text = "Select car color",
                            color = Color(0xFFE91E63),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }

                    OutlinedTextField(
                        value = carNumber,
                        onValueChange = {
                            carNumber = it
                            carNumberError = false
                        },
                        placeholder = { Text("Car Number", color = Color(0xFF999999)) },
                        isError = carNumberError,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
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
                    if (carNumberError) {
                        Text(
                            text = "Enter car number",
                            color = Color(0xFFE91E63),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            carModelError = false
                            carTypeError = false
                            carColorError = false
                            carNumberError = false

                            var valid = true

                            if (carType.isBlank()) {
                                carTypeError = true; valid = false
                            }
                            if (carColor.isBlank()) {
                                carColorError = true; valid = false
                            }
                            if (carNumber.isBlank()) {
                                carNumberError = true; valid = false
                            }

                            if (valid) {
                                regViewModel.carType = carType
                                regViewModel.carColor = carColor
                                regViewModel.carNumber = carNumber

                                coroutineScope.launch {
                                    authViewModel.register(
                                        email = regViewModel.email,
                                        password = regViewModel.password,
                                        name = regViewModel.name,
                                        surname = regViewModel.surname,
                                        phone = regViewModel.phone,
                                        role = "driver",
                                        carType = regViewModel.carType,
                                        carColor = regViewModel.carColor,
                                        carNumber = regViewModel.carNumber,
                                        cityName = regViewModel.cityName,
                                        onSuccess = {
                                            navController.navigate("emailConfirmation/${regViewModel.email}") {
                                                popUpTo("welcome") { inclusive = true }
                                                launchSingleTop = true
                                            }
                                        },
                                        onError = { err ->
                                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                            regViewModel.role = "driver"
                                            navController.navigate("basicInfoReg")
                                        }
                                    )
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
                            text = "Next step",
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
