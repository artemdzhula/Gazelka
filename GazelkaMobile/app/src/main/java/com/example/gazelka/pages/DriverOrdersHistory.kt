package com.example.gazelka.pages

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.res.painterResource
import com.example.gazelka.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gazelka.AuthViewModel
import com.example.gazelka.models.Order

@Composable
fun DriverOrdersHistory(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current

    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        authViewModel.getDriverOrdersHistory(
            onSuccess = { list ->
                orders = list
                isLoading = false
            },
            onError = { err ->
                error = err
                isLoading = false
                Toast.makeText(context, "Error: $err", Toast.LENGTH_LONG).show()
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
                .background(Color(0xFF000000).copy(alpha = 0.15f))
        )

        Scaffold(
            bottomBar = {
                DriverBottomNavigationBar(navController = navController, currentRoute = "driverHistory")
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))

                when {
                    isLoading -> {
                        CircularProgressIndicator()
                    }
                    error != null -> {
                        Text("Failed to load orders: $error", color = Color(0xFF000000), fontSize = 16.sp)
                    }
                    orders.isEmpty() -> {
                        Text("No orders in history", fontSize = 16.sp, color = Color(0xFF000000), fontFamily = PoppinsFontFamily)
                    }
                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(orders) { order ->
                                DriverOrderHistoryCard(
                                    order = order,
                                    navController = navController
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DriverOrderHistoryCard(
    order: Order,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("DriverOrderDetails/${order.orderNumber}")
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Order #${order.orderNumber}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF9C80E),
                        fontFamily = PoppinsFontFamily
                    )
                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Date: ${order.date} ${order.time}",
                        fontSize = 12.sp,
                        color = Color(0xFFCCCCCC),
                        fontFamily = PoppinsFontFamily
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "From: ",
                            fontSize = 12.sp,
                            color = Color(0xFFFFFFFF),
                            fontFamily = PoppinsFontFamily
                        )
                        Text(
                            text = order.from ?: "",
                            fontSize = 12.sp,
                            color = Color(0xFFFFFFFF),
                            fontFamily = PoppinsFontFamily,
                            modifier = Modifier
                                .weight(1f)
                                .basicMarquee(
                                    animationMode = MarqueeAnimationMode.Immediately
                                ),
                            maxLines = 1
                        )
                    }


                    Spacer(Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "To: ",
                            fontSize = 12.sp,
                            color = Color(0xFFFFFFFF),
                            fontFamily = PoppinsFontFamily
                        )
                        Text(
                            text = order.to ?: "",
                            fontSize = 12.sp,
                            color = Color(0xFFFFFFFF),
                            fontFamily = PoppinsFontFamily,
                            modifier = Modifier
                                .weight(1f)
                                .basicMarquee(
                                    animationMode = MarqueeAnimationMode.Immediately
                                ),
                            maxLines = 1
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "Distance: ${order.distance?.toInt()} km",
                        fontSize = 12.sp,
                        color = Color(0xFFFFFFFF),
                        fontFamily = PoppinsFontFamily,
                        maxLines = 1,
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Vehicle type: ${order.vehicleType}",
                        fontSize = 12.sp,
                        color = Color(0xFFCCCCCC),
                        fontFamily = PoppinsFontFamily
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(start = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(
                        modifier = Modifier
                            .width(2.dp)
                            .height(80.dp)
                            .background(Color(0xFFF9C80E))
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(start = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Price:",
                        fontSize = 10.sp,
                        color = Color(0xFFCCCCCC),
                        fontFamily = PoppinsFontFamily
                    )
                    Text(
                        text = "${order.price?.toInt()}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF9C80E),
                        fontFamily = PoppinsFontFamily
                    )
                    Text(
                        text = "USD",
                        fontSize = 12.sp,
                        color = Color(0xFFCCCCCC),
                        fontFamily = PoppinsFontFamily
                    )
                }
            }

            StatusBar(status = order.status)
        }
    }
}


