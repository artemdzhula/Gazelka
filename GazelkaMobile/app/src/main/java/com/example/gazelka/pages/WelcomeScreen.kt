package com.example.gazelka.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gazelka.AuthViewModel
import com.example.gazelka.R


val PoppinsFontFamily = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

@Composable
fun WelcomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {


    LaunchedEffect(Unit) {
        authViewModel.tryAutoLogin(
            onSuccess = { role ->
                val userId = authViewModel._userData.value?.id?: null
                if (userId == null || role == null) {
                    return@tryAutoLogin
                }
                if (role == "driver") {
                    navController.navigate("availableOrders") {
                        popUpTo(0)
                    }
                } else {
                    navController.navigate("customerScheduledOrders") {
                        popUpTo(0)
                    }
                }
            },
            onFailure = {
            }
        )
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
                .background(Color(0xFFF9C80E).copy(alpha = 0.15f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, start = 32.dp, end = 32.dp, bottom = 0.dp),

                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Application logo",
                        modifier = Modifier.size(130.dp)
                    )
                }


                Text(
                    text = "WELCOME !",
                    fontFamily = PoppinsFontFamily,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF000000),
                    letterSpacing = 2.sp
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A1A)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(28.dp)
                ) {

                    Image(
                        painter = painterResource(id = R.drawable.ic_create_order),
                        contentDescription = "Track delivery",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Create orders easily.",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF9C80E),
                        lineHeight = 26.sp
                    )
                    Text(
                        text = "Choose your pickup and drop-off points, set the date and time, and we'll find the right vehicle for you.",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFFF9C80E),
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))


                    Image(
                        painter = painterResource(id = R.drawable.ic_track_delivery),
                        contentDescription = "Track delivery",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Track your delivery.",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF9C80E),
                        lineHeight = 26.sp
                    )
                    Text(
                        text = "View your orders, check their status, and see all route details.",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFFF9C80E),
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))


                    Image(
                        painter = painterResource(id = R.drawable.ic_become_driver),
                        contentDescription = "Track delivery",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Become a driver and earn.",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF9C80E),
                        lineHeight = 26.sp
                    )
                    Text(
                        text = "Driver registration takes just a few minutes: set up your vehicle and start accepting orders.",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFFF9C80E),
                        lineHeight = 22.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 34.dp)
            ) {
                Button(
                    onClick = {
                        navController.navigate("authLandingScreen")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1A1A1A)
                    )
                ) {
                    Text(
                        fontFamily = PoppinsFontFamily,
                        text = "START",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF9C80E),
                        letterSpacing = 3.sp
                    )
                }
            }
        }
    }
}
