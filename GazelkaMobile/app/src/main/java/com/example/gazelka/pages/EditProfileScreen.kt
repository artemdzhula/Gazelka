package com.example.gazelka.pages

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gazelka.AuthViewModel

@Composable
fun EditProfileScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val userData by authViewModel.userData.collectAsState()

    var name by remember { mutableStateOf(userData?.name ?: "") }
    var surname by remember { mutableStateOf(userData?.surname ?: "") }
    var email by remember { mutableStateOf(userData?.email ?: "") }
    var phone by remember { mutableStateOf(userData?.phoneNumber ?: "") }

    var carType by remember { mutableStateOf(userData?.carType ?: "") }
    var carColor by remember { mutableStateOf(userData?.carColor ?: "") }
    var carNumber by remember { mutableStateOf(userData?.carNumber ?: "") }
    var cityName by remember { mutableStateOf(userData?.cityName ?: "") }

    var nameError by remember { mutableStateOf(false) }
    var surnameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Edit Profile", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Divider()

        Spacer(Modifier.height(12.dp))
        // User Info
        Text("User info:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        @Composable
        fun textFieldError(value: String, onValueChange: (String) -> Unit, label: String, errorFlag: Boolean, onErrorFlag: (Boolean) -> Unit) {
            OutlinedTextField(
                value = value,
                onValueChange = { onValueChange(it); onErrorFlag(false) },
                label = { Text(label) },
                isError = errorFlag,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (errorFlag)
                Text("Enter $label", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }

        textFieldError(name, { name = it }, "Name", nameError) { nameError = it }
        Spacer(Modifier.height(8.dp))
        textFieldError(surname, { surname = it }, "Surname", surnameError) { surnameError = it }
        Spacer(Modifier.height(8.dp))
        textFieldError(email, { email = it }, "Email", emailError) { emailError = it }
        Spacer(Modifier.height(8.dp))
        textFieldError(phone, { phone = it }, "Phone", phoneError) { phoneError = it }

        Spacer(Modifier.height(16.dp))
        Divider()
        Spacer(Modifier.height(12.dp))

        // Car info
        if (userData?.role?.lowercase() == "driver") {
            Text("Car info:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            // Car Type Dropdown
            val carOptions = listOf(
                "Small Van — 3 m³" to "Small Van",
                "Medium Van — 6 m³" to "Medium Van",
                "Large Van — 12 m³" to "Large Van",
                "Luton Van — 20 m³" to "Luton Van",
            )
            var carTypeLabel by remember { mutableStateOf(carOptions.find { it.second == carType }?.first ?: "") }
            var expandedType by remember { mutableStateOf(false) }

            Box(modifier = Modifier.fillMaxWidth().clickable { expandedType = true }) {
                OutlinedTextField(
                    value = carTypeLabel,
                    onValueChange = {},
                    label = { Text("Car Type") },
                    readOnly = true,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(
                    expanded = expandedType,
                    onDismissRequest = { expandedType = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    carOptions.forEach { (label, value) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                carTypeLabel = label
                                carType = value
                                expandedType = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))

            // Car Color Dropdown
            val carColors = listOf("Red", "Green", "Blue", "White", "Black")
            var expandedColor by remember { mutableStateOf(false) }

            Box(modifier = Modifier.fillMaxWidth().clickable { expandedColor = true }) {
                OutlinedTextField(
                    value = carColor,
                    onValueChange = {},
                    label = { Text("Car Color") },
                    readOnly = true,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )
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
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))

            // Car Number
            OutlinedTextField(
                value = carNumber,
                onValueChange = { carNumber = it },
                label = { Text("Car Number") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
        }



        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                var valid = true
                if (name.isBlank()) { nameError = true; valid = false }
                if (surname.isBlank()) { surnameError = true; valid = false }
                if (email.isBlank()) { emailError = true; valid = false }
                if (phone.isBlank()) { phoneError = true; valid = false }

                if (valid) {
                    authViewModel.updateProfile(
                        name = name,
                        surname = surname,
                        email = email,
                        phone = phone,
                        carType = carType,
                        carColor = carColor,
                        carNumber = carNumber,
                        cityName = cityName,
                        onSuccess = {
                            Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        },
                        onError = { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Changes")
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { navController.popBackStack() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }

        Spacer(Modifier.height(16.dp))
    }
}
