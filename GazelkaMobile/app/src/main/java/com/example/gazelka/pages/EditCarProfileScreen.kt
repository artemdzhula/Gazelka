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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gazelka.AuthViewModel
import com.example.gazelka.R
import kotlin.collections.find
import kotlin.collections.forEach
import kotlin.collections.map
import kotlin.to

@Composable
fun EditCarProfileScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val user by authViewModel.userData.collectAsState()

    var carType by remember { mutableStateOf(user?.carType ?: "") }
    var carColor by remember { mutableStateOf(user?.carColor ?: "") }
    var carNumber by remember { mutableStateOf(user?.carNumber ?: "") }

    val carOptions = listOf(
        "Small Van — 3 m³" to "Small Van",
        "Medium Van — 6 m³" to "Medium Van",
        "Large Van — 12 m³" to "Large Van",
        "Luton Van — 20 m³" to "Luton Van"
    )

    var carTypeLabel by remember {
        mutableStateOf(carOptions.find { it.second == carType }?.first ?: "")
    }
    var expandedType by remember { mutableStateOf(false) }
    var expandedColor by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.wallpaper),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF000000).copy(alpha = 0.15f)))

        Scaffold(containerColor = Color.Transparent) { paddingValues ->
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
                        .verticalScroll(scrollState)
                ) {
                    // Top bar
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
                                text = "Edit Car Info",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = PoppinsFontFamily
                            )

                            Spacer(modifier = Modifier.size(40.dp))
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Form Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Car Info",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = PoppinsFontFamily
                            )
                            Spacer(Modifier.height(20.dp))

                            // Car Type Dropdown
                            CustomDropdownField(
                                value = carTypeLabel,
                                onClick = { expandedType = true },
                                leadingIcon = R.drawable.ic_car,
                                placeholder = "Select car type...",
                                expanded = expandedType,
                                onDismiss = { expandedType = false },
                                options = carOptions.map { it.first },
                                onOptionSelect = { label ->
                                    carTypeLabel = label
                                    carType = carOptions.find { it.first == label }?.second ?: ""
                                    expandedType = false
                                },
                                fontFamily = PoppinsFontFamily
                            )
                            Spacer(Modifier.height(16.dp))

                            // Car Number Field
                            CustomTextField(
                                value = carNumber,
                                onValueChange = { carNumber = it },
                                leadingIcon = R.drawable.ic_plate,
                                placeholder = "Enter car number...",
                                fontFamily = PoppinsFontFamily
                            )
                            Spacer(Modifier.height(16.dp))

                            // Car Color Dropdown
                            CustomDropdownField(
                                value = carColor,
                                onClick = { expandedColor = true },
                                leadingIcon = R.drawable.ic_color,
                                placeholder = "Select color...",
                                expanded = expandedColor,
                                onDismiss = { expandedColor = false },
                                options = listOf("Red", "Green", "Blue", "White", "Black"),
                                onOptionSelect = { color ->
                                    carColor = color
                                    expandedColor = false
                                },
                                fontFamily = PoppinsFontFamily
                            )
                            Spacer(Modifier.height(24.dp))

                            // Save button
                            Button(
                                onClick = {
                                    authViewModel.updateProfile(
                                        name = user!!.name,
                                        surname = user!!.surname,
                                        email = user!!.email,
                                        phone = user!!.phoneNumber,
                                        carType = carType,
                                        carColor = carColor,
                                        carNumber = carNumber,
                                        cityName = user?.cityName,
                                        onSuccess = {
                                            Toast.makeText(context, "Car info updated!", Toast.LENGTH_SHORT).show()
                                            navController.popBackStack()
                                        },
                                        onError = { msg ->
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9C80E)),
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) {
                                Text(
                                    text = "Save Changes",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF000000),
                                    fontFamily = PoppinsFontFamily
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
private fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: Int,
    placeholder: String,
    fontFamily: FontFamily
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        leadingIcon = {
            Icon(
                painter = painterResource(id = leadingIcon),
                contentDescription = null,
                tint = Color(0xFF666666),
                modifier = Modifier.size(20.dp)
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                fontFamily = fontFamily,
                fontSize = 14.sp,
                color = Color(0xFF999999)
            )
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = Color(0xFFF9C80E),
            unfocusedIndicatorColor = Color(0xFF333333),
            focusedTextColor = Color(0xFF000000),
            unfocusedTextColor = Color(0xFF000000),
            cursorColor = Color(0xFFF9C80E)
        )
    )
}

@Composable
private fun CustomDropdownField(
    value: String,
    onClick: () -> Unit,
    leadingIcon: Int,
    placeholder: String,
    expanded: Boolean,
    onDismiss: () -> Unit,
    options: List<String>,
    onOptionSelect: (String) -> Unit,
    fontFamily: FontFamily
) {
    Box(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            leadingIcon = {
                Icon(
                    painter = painterResource(id = leadingIcon),
                    contentDescription = null,
                    tint = Color(0xFF666666),
                    modifier = Modifier.size(20.dp)
                )
            },
            placeholder = {
                Text(
                    text = placeholder,
                    fontFamily = fontFamily,
                    fontSize = 14.sp,
                    color = Color(0xFF999999)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color(0xFFF9C80E),
                unfocusedIndicatorColor = Color(0xFF333333),
                focusedTextColor = Color(0xFF000000),
                unfocusedTextColor = Color(0xFF000000)
            )
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, fontFamily = fontFamily) },
                    onClick = { onOptionSelect(option) }
                )
            }
        }
    }
}
