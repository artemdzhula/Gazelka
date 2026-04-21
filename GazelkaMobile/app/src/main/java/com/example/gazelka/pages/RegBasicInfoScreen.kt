package com.example.gazelka.pages

import android.app.Activity
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gazelka.AuthViewModel
import com.example.gazelka.R
import kotlinx.coroutines.launch
import com.example.gazelka.RegistrationViewModel
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.ui.draw.clip

fun Context.findActivity(): ComponentActivity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is ComponentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@Composable
fun RegBasicInfoScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    regViewModel: RegistrationViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var name by rememberSaveable { mutableStateOf("") }
    var surname by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    var nameError by remember { mutableStateOf(false) }
    var surnameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var cityError by remember { mutableStateOf(false) }

    val status by authViewModel.status.collectAsState(initial = null)

    val cityPickerLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val place = Autocomplete.getPlaceFromIntent(result.data!!)
                regViewModel.cityName = place.name ?: ""
            }
        }

    LaunchedEffect(status) {
        status?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
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
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = false
                        },
                        placeholder = { Text("Name", color = Color(0xFF999999)) },
                        isError = nameError,
                        singleLine = true,
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
                    if (nameError) {
                        Text(
                            text = "Enter valid name",
                            color = Color(0xFFE91E63),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }

                    OutlinedTextField(
                        value = surname,
                        onValueChange = {
                            surname = it
                            surnameError = false
                        },
                        placeholder = { Text("Surname", color = Color(0xFF999999)) },
                        isError = surnameError,
                        singleLine = true,
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
                    if (surnameError) {
                        Text(
                            text = "Enter valid surname",
                            color = Color(0xFFE91E63),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = false
                        },
                        placeholder = { Text("Email", color = Color(0xFF999999)) },
                        isError = emailError,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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
                    if (emailError) {
                        Text(
                            text = "Enter valid email (example@email.com)",
                            color = Color(0xFFE91E63),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }


                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = false
                        },
                        placeholder = { Text("Password", color = Color(0xFF999999)) },
                        isError = passwordError,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation(),
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
                    if (passwordError) {
                        Text(
                            text = "Password must be 8+ characters",
                            color = Color(0xFFE91E63),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }

                    if(regViewModel.role == "driver") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .clickable {
                                    val activity = context.findActivity()
                                    if (activity != null) cityPickerLauncher.launch(
                                        createCityPickerIntent(activity)
                                    )
                                },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8E8E8)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = if (regViewModel.cityName.isEmpty()) "City" else regViewModel.cityName,
                                    fontSize = 14.sp,
                                    color = if (regViewModel.cityName.isEmpty()) Color(0xFF999999) else Color(
                                        0xFF000000
                                    )
                                )
                            }
                        }
                        if (cityError) {
                            Text(
                                text = "Select your city",
                                color = Color(0xFFE91E63),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            nameError = false
                            surnameError = false
                            emailError = false
                            passwordError = false
                            cityError = false

                            var valid = true
                            val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

                            if (name.isBlank()) {
                                nameError = true; valid = false
                            }
                            if (surname.isBlank()) {
                                surnameError = true; valid = false
                            }
                            if (email.isBlank() || !emailRegex.matches(email.trim())) {
                                emailError = true; valid = false
                            }
                            if (password.length < 8) {
                                passwordError = true; valid = false
                            }
                            if (regViewModel.cityName.isBlank() && regViewModel.role == "driver") {
                                cityError = true; valid = false
                            }

                            if (valid) {
                                regViewModel.name = name.trim()
                                regViewModel.surname = surname.trim()
                                regViewModel.email = email.trim()
                                regViewModel.password = password.trim()
                                navController.navigate("phoneInfoReg")
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

fun createCityPickerIntent(activity: ComponentActivity) =
    Autocomplete.IntentBuilder(
        AutocompleteActivityMode.FULLSCREEN,
        listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS_COMPONENTS
        )
    )
        .setTypesFilter(listOf("locality"))
        .build(activity)
