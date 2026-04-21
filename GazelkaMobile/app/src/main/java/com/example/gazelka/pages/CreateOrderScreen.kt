package com.example.gazelka.pages

import android.widget.Toast
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import java.util.Calendar
import com.google.android.gms.maps.model.LatLng
import android.content.Context
import android.location.Geocoder
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.basicMarquee
import java.util.Locale
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject





data class VehicleType(
    val id: String,
    val name: String,
    val capacity: String,
    val weight: String,
    val imageRes: Int,
    val examples: String
)

val vehicleRates = mapOf(
    "Small Van" to 3.0,
    "Medium Van" to 4.0,
    "Large Van" to 5.0,
    "Luton Van" to 6.0
)

val standardFee = 20.0
val valuableFee = 10.0
val fragileFee = 10.0
val heavyFee = 15.0
val minPrice = 50.0




suspend fun getDistanceKm(pointA: String, pointB: String): Double? {
    return withContext(Dispatchers.IO) {
        try {
            val url = "https://maps.googleapis.com/maps/api/directions/json" +
                    "?origin=${pointA}" +
                    "&destination=${pointB}" +
                    "&key=AIzaSyAfKzmjZZZLzrEVa8wtb8GII8vIn0VcHfA"

            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext null

            val json = JSONObject(body)
            val routes = json.getJSONArray("routes")
            if (routes.length() == 0) return@withContext null

            val legs = routes.getJSONObject(0).getJSONArray("legs")
            val distanceMeters = legs.getJSONObject(0).getJSONObject("distance").getDouble("value")

            distanceMeters / 1000.0
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}


fun latLngToAddress(context: Context, latLng: LatLng): String {
    val geocoder = Geocoder(context, Locale.getDefault())
    if (!Geocoder.isPresent()) return "Unknown address"
    val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
    return addresses?.firstOrNull()?.getAddressLine(0) ?: "Unknown address"
}


fun calculatePrice(
    distanceKm: Double,
    vehicleType: String,
    standard: Boolean,
    valuable: Boolean,
    fragile: Boolean,
    heavy: Boolean
): Double {
    val rate = vehicleRates[vehicleType] ?: throw IllegalArgumentException("Unknown vehicle type")
    var price = distanceKm * rate

    if (standard) price += standardFee
    if (valuable) price += valuableFee
    if (fragile) price += fragileFee
    if (heavy) price += heavyFee

    price = maxOf(minPrice, price)
    return kotlin.math.ceil(price)
}

fun isAtLeastOneHourLater(orderDate: String, orderTime: String): Boolean {
    return try {
        val dateParts = orderDate.split("/")
        val timeParts = orderTime.split(":")

        val orderCalendar = Calendar.getInstance().apply {
            set(
                dateParts[2].toInt(),      // year
                dateParts[1].toInt() - 1,  // month (0-based)
                dateParts[0].toInt(),      // day
                timeParts[0].toInt(),      // hour
                timeParts[1].toInt(),      // minute
                0
            )
        }

        val nowPlusHour = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 1)
        }

        orderCalendar.after(nowPlusHour)
    } catch (e: Exception) {
        false
    }
}
fun getTodayDate(): String {
    val cal = Calendar.getInstance()
    return "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.YEAR)}"
}






@Composable
fun CreateOrderScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pointA by remember { mutableStateOf("") }
    var pointB by remember { mutableStateOf("") }
    var selectedVehicle by remember { mutableStateOf<VehicleType?>(null) }
    var orderDate by remember { mutableStateOf("") }
    var orderTime by remember { mutableStateOf("") }
    var selectingPoint by remember { mutableStateOf<String?>(null) }
    var isMapInteracting by remember { mutableStateOf(false) }
    var optionStandard by remember { mutableStateOf(true) }
    var optionValuable by remember { mutableStateOf(false) }
    var optionFragile by remember { mutableStateOf(false) }
    var optionHeavy by remember { mutableStateOf(false) }
    var estimatedPrice by remember { mutableStateOf(0.0) }


    LaunchedEffect(pointA, pointB, selectedVehicle, optionStandard, optionValuable, optionFragile, optionHeavy) {
        if (pointA.isNotBlank() && pointB.isNotBlank() && selectedVehicle != null) {
            val distance = getDistanceKm(pointA, pointB)
            estimatedPrice = calculatePrice(
                distanceKm = distance!!,
                vehicleType = selectedVehicle!!.id,
                standard = optionStandard,
                valuable = optionValuable,
                fragile = optionFragile,
                heavy = optionHeavy
            )
        }
    }
    val vehicles = listOf(
        VehicleType(
            id = "Small Van",
            name = "Small van",
            capacity = "3–6 m²",
            weight = "600kg",
            imageRes = R.drawable.small_van,
            examples = "VW Caddy, Renault Kangoo, etc."
        ),
        VehicleType(
            id = "Medium Van",
            name = "Medium van",
            capacity = "8–12 m²",
            weight = "1000kg",
            imageRes = R.drawable.medium_van,
            examples = "Ford Transit Custom, Mercedes Vito, etc."
        ),
        VehicleType(
            id = "Large Van",
            name = "Large van",
            capacity = "14–22 m²",
            weight = "1500kg",
            imageRes = R.drawable.large_van,
            examples = "Mercedes Sprinter, Peugeot Boxer, etc."
        ),
        VehicleType(
            id = "Luton Van",
            name = "Luton Van",
            capacity = "27–40 m²",
            weight = "3500kg",
            imageRes = R.drawable.truck_van,
            examples = "MAN TGL, Mercedes Atego, etc."
        )
    )

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
                CustomerBottomNavigationBar(
                    navController = navController,
                    currentRoute = "createOrder"
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A2A2A).copy(alpha = 0.90f)
                ),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top,
                    userScrollEnabled = !isMapInteracting
                ) {
                    item {
                        Spacer(Modifier.height(8.dp))
                    }

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(28.dp))
                                .clickable { selectingPoint = "A" },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (pointA.isEmpty()) "From:" else pointA,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1A1A1A),
                                    fontFamily = PoppinsFontFamily,
                                    modifier = Modifier
                                        .weight(1f)
                                        .basicMarquee(),
                                    maxLines = 1
                                )

                                Icon(
                                    painter = painterResource(id = R.drawable.ic_location),
                                    contentDescription = "Select location",
                                    tint = Color(0xFF4A4A4A),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // To
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(28.dp))
                                .clickable { selectingPoint = "B" },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (pointB.isEmpty()) "To:" else pointB,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1A1A1A),
                                    fontFamily = PoppinsFontFamily,
                                    modifier = Modifier
                                        .weight(1f)
                                        .basicMarquee(),
                                    maxLines = 1
                                )
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_location),
                                    contentDescription = "Select location",
                                    tint = Color(0xFF4A4A4A),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                    }

                    if (selectingPoint != null) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                SelectLocationMap(
                                    modifier = Modifier.fillMaxSize(),
                                    onLocationSelected = { latLng ->
                                            val address = latLngToAddress(context, latLng)

                                            if (selectingPoint == "A") {
                                                pointA = address
                                            } else {
                                                pointB = address
                                            }
                                            selectingPoint = null

                                    },
                                    onInteractionChanged = { interacting ->
                                        isMapInteracting = interacting
                                    }
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        val calendar = Calendar.getInstance()
                                        val dialog = DatePickerDialog(
                                            context,
                                            { _, year, month, day ->
                                                orderDate = "$day/${month + 1}/$year"
                                            },
                                            calendar.get(Calendar.YEAR),
                                            calendar.get(Calendar.MONTH),
                                            calendar.get(Calendar.DAY_OF_MONTH)
                                        )

                                        dialog.datePicker.minDate = System.currentTimeMillis()

                                        dialog.show()

                                    },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (orderDate.isEmpty()) "Date" else orderDate,
                                        fontSize = 14.sp,
                                        color = Color(0xFFFFFFFF),
                                        fontFamily = PoppinsFontFamily
                                    )
                                }
                            }

                            // Time
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        val calendar = Calendar.getInstance()
                                        TimePickerDialog(
                                            context,
                                            { _, hour, minute ->
                                                val picked = Calendar.getInstance().apply {
                                                    set(Calendar.HOUR_OF_DAY, hour)
                                                    set(Calendar.MINUTE, minute)
                                                    set(Calendar.SECOND, 0)
                                                }

                                                val nowPlusHour = Calendar.getInstance().apply {
                                                    add(Calendar.HOUR_OF_DAY, 1)
                                                }

                                                if (picked.before(nowPlusHour) && orderDate == getTodayDate()) {
                                                    Toast.makeText(
                                                        context,
                                                        "Select time at least 1 hour from now",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    orderTime = "%02d:%02d".format(hour, minute)
                                                }
                                            },
                                            calendar.get(Calendar.HOUR_OF_DAY),
                                            calendar.get(Calendar.MINUTE),
                                            true
                                        ).show()

                                    },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (orderTime.isEmpty()) "Time" else orderTime,
                                        fontSize = 14.sp,
                                        color = Color(0xFFFFFFFF),
                                        fontFamily = PoppinsFontFamily
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    item {
                        // Vehicle category
                        Text(
                            "Vehicle category:",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFFFFF),
                            fontFamily = PoppinsFontFamily
                        )
                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            vehicles.forEach { vehicle ->
                                VehicleCard(
                                    vehicle = vehicle,
                                    isSelected = selectedVehicle?.id == vehicle.id,
                                    onClick = { selectedVehicle = vehicle }
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    item {
                        // Cargo category
                        Text(
                            "Cargo category:",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFFFFF),
                            fontFamily = PoppinsFontFamily
                        )
                        Spacer(Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CargoCheckbox(
                                    modifier = Modifier.weight(1f),
                                    isChecked = optionStandard,
                                    onCheckedChange = { optionStandard = it },
                                    title = "Standard",
                                    description = "Furniture, boxes, equipment"
                                )
                                CargoCheckbox(
                                    modifier = Modifier.weight(1f),
                                    isChecked = optionValuable,
                                    onCheckedChange = { optionValuable = it },
                                    title = "Valuable",
                                    description = "Electronics, tools, etc."
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CargoCheckbox(
                                    modifier = Modifier.weight(1f),
                                    isChecked = optionFragile,
                                    onCheckedChange = { optionFragile = it },
                                    title = "Fragile",
                                    description = "Tableware, mirrors, glass"
                                )
                                CargoCheckbox(
                                    modifier = Modifier.weight(1f),
                                    isChecked = optionHeavy,
                                    onCheckedChange = { optionHeavy = it },
                                    title = "Heavy",
                                    description = "Refrigerator, piano, etc."
                                )
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                    }
                    if (estimatedPrice > 0.0) {
                        item {
                            Text(
                                text = "Estimated price: $${estimatedPrice.toInt()}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFC107),
                                modifier = Modifier.padding(top = 16.dp)
                            )
                            Spacer(Modifier.height(20.dp))
                        }
                    }

                    item {
                        // Accept Order button
                        Button(
                            onClick = {
                                if (pointA.isBlank() || pointB.isBlank() || selectedVehicle == null || orderDate.isBlank() || orderTime.isBlank()) {
                                    Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                if (!isAtLeastOneHourLater(orderDate, orderTime)) {
                                    Toast.makeText(
                                        context,
                                        "Order must be scheduled at least 1 hour from now",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return@Button
                                }


                                authViewModel.createOrder(
                                    pointA = pointA,
                                    pointB = pointB,
                                    vehicleType = selectedVehicle?.id ?: "",
                                    date = orderDate,
                                    time = orderTime,
                                    standard = optionStandard,
                                    valuable = optionValuable,
                                    fragile = optionFragile,
                                    heavy = optionHeavy,
                                    onSuccess = {
                                        Toast.makeText(context, "Order created!", Toast.LENGTH_SHORT).show()
                                        navController.navigate("customerScheduledOrders") {
                                            popUpTo("createOrder") { inclusive = true }
                                        }
                                    },
                                    onError = { err ->
                                        Toast.makeText(context, "Error: $err", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(67.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9C80E)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Create order",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A),
                                fontFamily = PoppinsFontFamily
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}



