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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.example.gazelka.models.Order
import com.example.gazelka.models.UserData
import kotlinx.serialization.json.Json
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.text.style.TextAlign


@Composable
fun DriverOrderDetailsScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    orderId: Int
) {
    val context = LocalContext.current
    var order by remember { mutableStateOf<Order?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var customer by remember { mutableStateOf<UserData?>(null) }
    var driverAction by remember { mutableStateOf<DriverAction?>(null) }
    var canCancel = remember { mutableStateOf(false) }
    var canAccept = remember { mutableStateOf(false) }
    var buttonText by remember { mutableStateOf("") }

    fun getStatusColor(status: String?): Color {
        return when (status) {
            "Accepted" -> Color(0xFF48D700)
            "Driver Coming", "Picking up", "Delivering" -> Color(0xFF00BCD4)
            "Completed", "Canceled" -> Color(0xFFCD0000)
            else -> Color(0xFFf7d794)
        }
    }

    fun reloadOrder() {
        loading = true
        authViewModel.getOrderById(
            orderId = orderId,
            onSuccess = {
                order = it
                driverAction = getDriverAction(it)
                canCancel.value = it.status == "Accepted"
                canAccept.value = it.status == "Pending"
                buttonText = if (canAccept.value) "Accept Order" else "Cancel Order"
                loading = false
            },
            onError = {
                loading = false
            }
        )
    }

    LaunchedEffect(orderId) {
        authViewModel.getOrderById(
            orderId = orderId,
            onSuccess = {
                order = it
                loading = false
                driverAction = getDriverAction(order!!)
                canCancel.value = it.status == "Accepted"
                canAccept.value = it.status == "Pending"
                buttonText = if (canAccept.value) "Accept Order" else "Cancel Order"
            },
            onError = {
                error = it
                loading = false
            }
        )
    }

    LaunchedEffect(order) {
        order?.customerId?.let { customerId ->
            customer = authViewModel.getUserDataById(customerId)
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
                            text = "Order Details",
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
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                ) {
                    if (order == null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Error loading order",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    } else {
                        Spacer(Modifier.height(16.dp))

                        // Order Number & Status
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Order #${order?.orderNumber ?: "N/A"}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    fontFamily = PoppinsFontFamily
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = order?.status ?: "Pending",
                                    fontSize = 12.sp,
                                    color = getStatusColor(order?.status),
                                    fontFamily = PoppinsFontFamily
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Date & Time
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Date and Time:",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    fontFamily = PoppinsFontFamily
                                )
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 8.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = order?.date ?: "N/A",
                                                fontSize = 12.sp,
                                                color = Color.White,
                                                fontFamily = PoppinsFontFamily
                                            )
                                        }
                                    }

                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 8.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = order?.time ?: "N/A",
                                                fontSize = 12.sp,
                                                color = Color.White,
                                                fontFamily = PoppinsFontFamily
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Trip
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Trip:",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    fontFamily = PoppinsFontFamily
                                )
                                Spacer(Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = order?.from ?: "Pickup location",
                                        fontSize = 12.sp,
                                        color = Color(0xFFB0B0B0),
                                        modifier = Modifier.clickable {
                                            val fromAddress = order?.from ?: return@clickable
                                            val clipboard =
                                                context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            clipboard.setPrimaryClip(
                                                ClipData.newPlainText(
                                                    "Address",
                                                    fromAddress
                                                )
                                            )
                                            Toast.makeText(
                                                context,
                                                "Pickup copied",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        textAlign = TextAlign.Center,
                                        fontFamily = PoppinsFontFamily
                                    )
                                }

                                Spacer(Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_arrow_down),
                                        contentDescription = "Route",
                                        tint = Color(0xFFF9C80E),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "${order?.distance?.toInt() ?: "0"} км",
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = PoppinsFontFamily
                                    )
                                }

                                Spacer(Modifier.height(12.dp))

                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = order?.to ?: "Delivery location",
                                        fontSize = 12.sp,
                                        color = Color(0xFFB0B0B0),
                                        modifier = Modifier.clickable {
                                            val toAddress = order?.to ?: return@clickable
                                            val clipboard =
                                                context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            clipboard.setPrimaryClip(
                                                ClipData.newPlainText(
                                                    "Address",
                                                    toAddress
                                                )
                                            )
                                            Toast.makeText(
                                                context,
                                                "Delivery copied",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        textAlign = TextAlign.Center,
                                        fontFamily = PoppinsFontFamily
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // CARGO
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Cargo:",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    fontFamily = PoppinsFontFamily
                                )
                                Spacer(Modifier.height(12.dp))

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = order?.cargoOptions?.joinToString(", ") ?: "No options",
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            fontFamily = PoppinsFontFamily
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Customer info
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Customer:",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    fontFamily = PoppinsFontFamily
                                )
                                Spacer(Modifier.height(12.dp))

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = "${customer?.name ?: "N/A"} ${customer?.surname ?: ""}",
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            fontFamily = PoppinsFontFamily
                                        )
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val phone = customer?.phoneNumber ?: return@clickable
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Phone", phone))
                                            Toast.makeText(context, "Phone copied to clipboard", Toast.LENGTH_SHORT).show()
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = customer?.phoneNumber ?: "N/A",
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            fontFamily = PoppinsFontFamily
                                        )
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val email = customer?.email ?: return@clickable
                                            try {
                                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                                    data = Uri.parse("mailto:")
                                                    putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                                                }
                                                val chooser = Intent.createChooser(intent, "Send email")
                                                context.startActivity(chooser)
                                            } catch (e: Exception) {
                                            }
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = customer?.email ?: "N/A",
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            fontFamily = PoppinsFontFamily
                                        )
                                    }
                                }

                            }
                        }
                    }
                }
            }

            if (order != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 52.dp
                        )
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Price : ",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontFamily = PoppinsFontFamily
                                )
                                Text(
                                    text = "${order!!.price?.toInt() ?: " 0"}$",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF48D700),
                                    fontFamily = PoppinsFontFamily
                                )
                            }

                            driverAction?.let { action ->
                                Button(
                                    onClick = {
                                        when (action.action) {
                                            DriverOrderAction.COMING -> {
                                                authViewModel.comingOrder(
                                                    orderId = order!!.orderNumber,
                                                    onSuccess = { reloadOrder() },
                                                    onError = {
                                                        Toast.makeText(
                                                            context,
                                                            it,
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }
                                                )
                                            }
                                            DriverOrderAction.PICKING -> {
                                                authViewModel.pickingOrder(
                                                    orderId = order!!.orderNumber,
                                                    onSuccess = { reloadOrder() },
                                                    onError = {
                                                        Toast.makeText(
                                                            context,
                                                            it,
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }
                                                )
                                            }
                                            DriverOrderAction.DELIVERING -> {
                                                authViewModel.deliveringOrder(
                                                    orderId = order!!.orderNumber,
                                                    onSuccess = { reloadOrder() },
                                                    onError = {
                                                        Toast.makeText(
                                                            context,
                                                            it,
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }
                                                )
                                            }
                                            DriverOrderAction.COMPLETED -> {
                                                authViewModel.completeOrder(
                                                    orderId = order!!.orderNumber,
                                                    onSuccess = {
                                                        Toast.makeText(
                                                            context,
                                                            "Order completed",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        navController.popBackStack()
                                                    },
                                                    onError = {
                                                        Toast.makeText(
                                                            context,
                                                            it,
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }
                                                )
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFF9C80E)
                                    )
                                ) {
                                    Text(
                                        action.title,
                                        color = Color.Black,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        fontFamily = PoppinsFontFamily
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if(!canCancel.value && !canAccept.value) return@Button
                                        if(canCancel.value) {
                                            authViewModel.cancelOrder(
                                                orderId = order!!.orderNumber,
                                                onSuccess = {
                                                    Toast.makeText(
                                                        context,
                                                        "Order canceled",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    navController.navigate("DriverScheduledOrders") {
                                                        popUpTo("DriverScheduledOrders") {
                                                            inclusive = true
                                                        }
                                                    }
                                                },
                                                onError = { err ->
                                                    Toast.makeText(
                                                        context,
                                                        "Error: $err",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            )
                                        }
                                        if(canAccept.value){
                                            authViewModel.acceptOrder(
                                                orderId = order!!.orderNumber,
                                                onSuccess = {
                                                    Toast.makeText(
                                                        context,
                                                        "Order Accepted",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    navController.navigate("DriverScheduledOrders") {
                                                        popUpTo("availableOrders") {
                                                            inclusive = true
                                                        }
                                                    }
                                                },
                                                onError = { err ->
                                                    Toast.makeText(
                                                        context,
                                                        "Error: $err",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .alpha(if (canCancel.value || canAccept.value) 1f else 0.5f),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (canCancel.value)
                                            Color(0xFFCD0000)
                                        else if (canAccept.value)
                                            Color(0xFF4CAF50)
                                        else
                                            Color(0xFF6B6B6B),
                                        contentColor = if (canCancel.value || canAccept.value)
                                            Color.White
                                        else
                                            Color(0xFFB0B0B0)
                                    )
                                ) {
                                    Text(
                                        text = "$buttonText",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        fontFamily = PoppinsFontFamily
                                    )
                                }




                                Button(
                                    onClick = {
                                        navController.navigate("chat/${order?.orderNumber ?: ""}")
                                    },
                                    modifier = Modifier
                                        .size(48.dp),
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9C80E)),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_chat),
                                        contentDescription = "Chat",
                                        tint = Color.Black,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getDriverAction(order: Order): DriverAction? {
    return when (order.status) {
        "Accepted" -> DriverAction(
            title = "Go to Pickup",
            action = DriverOrderAction.COMING
        )
        "Driver Coming" -> DriverAction(
            title = "Start Picking",
            action = DriverOrderAction.PICKING
        )
        "Picking up" -> DriverAction(
            title = "Start Delivering",
            action = DriverOrderAction.DELIVERING
        )
        "Delivering" -> DriverAction(
            title = "Complete Order",
            action = DriverOrderAction.COMPLETED
        )
        else -> null
    }
}

private data class DriverAction(
    val title: String,
    val action: DriverOrderAction
)

private enum class DriverOrderAction {
    COMING,
    PICKING,
    DELIVERING,
    COMPLETED
}
