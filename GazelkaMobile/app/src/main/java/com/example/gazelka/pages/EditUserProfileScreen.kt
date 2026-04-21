package com.example.gazelka.pages

import android.app.Activity
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
import kotlin.text.isBlank
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import androidx.compose.ui.platform.LocalContext
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun EditUserProfileScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val user by authViewModel.userData.collectAsState()

    var name by remember { mutableStateOf(user?.name ?: "") }
    var surname by remember { mutableStateOf(user?.surname ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var phone by remember { mutableStateOf(user?.phoneNumber ?: "") }
    var city by remember { mutableStateOf(user?.cityName ?: "") }
    var nameError by remember { mutableStateOf(false) }
    var surnameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var oldEmail by remember { mutableStateOf(user?.email ?: "") }

    val cityPickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val place = Autocomplete.getPlaceFromIntent(result.data!!)
                city = place.name ?: ""
            }
        }

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
                                text = "Edit User Info",
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
                                text = "User Info",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = PoppinsFontFamily
                            )
                            Spacer(Modifier.height(20.dp))

                            // Name Field
                            CustomTextField(
                                value = name,
                                onValueChange = { name = it; nameError = false },
                                leadingIcon = R.drawable.ic_user,
                                error = nameError,
                                errorMessage = "Enter Name",
                                fontFamily = PoppinsFontFamily
                            )
                            Spacer(Modifier.height(16.dp))

                            // Surname Field
                            CustomTextField(
                                value = surname,
                                onValueChange = { surname = it; surnameError = false },
                                leadingIcon = R.drawable.ic_user,
                                error = surnameError,
                                errorMessage = "Enter Surname",
                                fontFamily = PoppinsFontFamily
                            )
                            Spacer(Modifier.height(16.dp))

                            // Email Field
                            CustomTextField(
                                value = email,
                                onValueChange = { email = it; emailError = false },
                                leadingIcon = R.drawable.ic_email,
                                error = emailError,
                                errorMessage = "Enter Email",
                                fontFamily = PoppinsFontFamily
                            )
                            Spacer(Modifier.height(16.dp))

                            // Phone Field
                            CustomTextField(
                                value = phone,
                                onValueChange = { phone = it; phoneError = false },
                                leadingIcon = R.drawable.ic_phone,
                                error = phoneError,
                                errorMessage = "Enter Phone",
                                fontFamily = PoppinsFontFamily
                            )
                            Spacer(Modifier.height(16.dp))

                            if (user?.role == "driver") {

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .clip(RoundedCornerShape(28.dp))
                                        .clickable {
                                            val activity = context.findActivity()
                                            if (activity != null) {
                                                cityPickerLauncher.launch(
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
                                                )
                                            }
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
                                            text = if (city.isEmpty()) "City" else city,
                                            fontSize = 14.sp,
                                            color = if (city.isEmpty()) Color(0xFF999999) else Color(0xFF000000)
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(24.dp))
                            // Save button
                            Button(
                                onClick = {
                                    var valid = true
                                    if (name.isBlank()) { nameError = true; valid = false }
                                    if (surname.isBlank()) { surnameError = true; valid = false }
                                    if (email.isBlank()) { emailError = true; valid = false }
                                    if (phone.isBlank()) { phoneError = true; valid = false }
                                    if (valid)
                                    {
                                        authViewModel.updateProfile(
                                            name = name,
                                            surname = surname,
                                            email = email,
                                            phone = phone,
                                            carType = user?.carType,
                                            carColor = user?.carColor,
                                            carNumber = user?.carNumber,
                                            cityName = if (user?.role == "driver") city else null,
                                            onSuccess = {
                                                Toast.makeText(context, "User info updated!", Toast.LENGTH_SHORT).show()
                                                if(email != oldEmail) {
                                                    authViewModel.resendEmail(
                                                        email = email,
                                                        onSuccess = {
                                                            Toast.makeText(
                                                                context,
                                                                "Code sent",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            navController.navigate("emailConfirmation/$email") {
                                                                popUpTo("editProfile") {
                                                                    inclusive = true
                                                                }
                                                            }
                                                        },
                                                        onError = { msg ->
                                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                        }
                                                    )
                                                }
                                                navController.popBackStack()
                                            },
                                            onError = { msg ->
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
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
    error: Boolean,
    errorMessage: String,
    fontFamily: FontFamily
) {
    Column {
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
                    text = "Enter text...",
                    fontFamily = fontFamily,
                    fontSize = 14.sp,
                    color = Color(0xFF999999)
                )
            },
            isError = error,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                errorContainerColor = Color.White,
                focusedIndicatorColor = Color(0xFFF9C80E),
                unfocusedIndicatorColor = Color(0xFF333333),
                errorIndicatorColor = Color(0xFFD32F2F),
                focusedTextColor = Color(0xFF000000),
                unfocusedTextColor = Color(0xFF000000),
                cursorColor = Color(0xFFF9C80E)
            )
        )

        if (error) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                color = Color(0xFFD32F2F),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = fontFamily,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}
