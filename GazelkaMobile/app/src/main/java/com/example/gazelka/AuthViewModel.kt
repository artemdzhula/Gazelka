package com.example.gazelka

import android.util.Base64
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import android.app.Application
import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.AndroidViewModel
import java.util.Calendar
import kotlinx.coroutines.withContext
import com.example.gazelka.models.Order
import com.example.gazelka.models.UserData
import com.example.gazelka.models.Message
import com.example.gazelka.models.Chat
import kotlinx.serialization.json.Json
import com.example.gazelka.signalr.ChatHubManager
import android.widget.Toast
import com.example.gazelka.models.NotificationSettings
import kotlinx.serialization.Serializable
import com.example.gazelka.signalr.MessageSignalR
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.update
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.MediaType.Companion.toMediaType
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Locale
import kotlin.collections.plusAssign
import kotlin.text.get


@Serializable
data class SendMessageRequest(
    val orderId: Int,
    val text: String
)



class AuthViewModel(
    application: Application,
    private val baseUrl: String = "http://10.0.2.2:5008/",
) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application.applicationContext)
    private val client = OkHttpClient()
    val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData

    val status =  MutableStateFlow<String?>(null)

    private val _chatMessages = MutableStateFlow<List<Message>>(emptyList())
    val chatMessages: StateFlow<List<Message>> = _chatMessages

    private val _currentChat = MutableStateFlow<Chat?>(null)
    private val _currentChatId = MutableStateFlow<Int?>(null)
    val currentChatId: StateFlow<Int?> = _currentChatId

    val chatHub = ChatHubManager(baseUrl) { tokenManager.accessToken }


    private val _notificationSettings = MutableStateFlow<NotificationSettings?>(null)
    val notificationSettings : StateFlow<NotificationSettings?> = _notificationSettings


    fun saveTokens(access: String, refresh: String?) {
        tokenManager.accessToken = access
        refresh?.let { tokenManager.refreshToken = it }
    }

    fun clearTokens() = tokenManager.clear()

    fun connectToChatHub() {
        viewModelScope.launch {
            chatHub.connect()
        }
    }

    fun refreshToken(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val rToken = tokenManager.refreshToken ?: run {
            onError("No refresh token")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val body = """{"refreshToken":"$rToken"}"""
                    .toRequestBody("application/json".toMediaTypeOrNull())

                val req = Request.Builder()
                    .url("${baseUrl}api/Auth/refresh")
                    .post(body)
                    .build()

                client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val text = resp.body.string()
                        val access = parseJsonField(text, "accessToken")
                        val refresh = parseJsonField(text, "refreshToken")
                        if (!access.isNullOrBlank()) {
                            saveTokens(access, refresh)
                            status.value = "Token refreshed"
                            onSuccess(access)
                        } else onError("Invalid response")
                    } else onError("HTTP ${resp.code}")
                }
            } catch (e: Exception) { onError(e.message ?: "Error") }
        }
    }

    fun logout(onComplete: () -> Unit) {
        val rToken = tokenManager.refreshToken ?: run { onComplete(); return }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val body = """{"refreshToken":"$rToken"}"""
                    .toRequestBody("application/json".toMediaTypeOrNull())

                val req = Request.Builder()
                    .url("${baseUrl}api/Auth/logout")
                    .post(body)
                    .build()

                client.newCall(req).execute().use { status.value = if (it.isSuccessful) "Logged out" else "Server error" }
            } catch (e: Exception) { status.value = e.message }
            finally { clearTokens(); withContext(Dispatchers.Main) { onComplete() } }
        }
    }

    private fun parseJsonField(json: String, field: String): String? {
        val pattern = """"$field"\s*:\s*"([^"]+)"""".toRegex()
        return pattern.find(json)?.groups?.get(1)?.value
    }


    fun login(
        email: String,
        password: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = """{"email":"$email","password":"$password"}"""
                val body = json.toRequestBody("application/json".toMediaTypeOrNull())
                val req = Request.Builder()
                    .url("${baseUrl}api/Auth/login")
                    .post(body)
                    .build()

                client.newCall(req).execute().use { resp ->
                    val responseBody = resp.body?.string() ?: ""

                    if (resp.isSuccessful) {
                        val access = parseJsonField(responseBody, "accessToken")
                        val refresh = parseJsonField(responseBody, "refreshToken")

                        if (!access.isNullOrBlank()) {
                            saveTokens(access, refresh)
                            val role = parseRoleFromToken(access)
                            status.value = "Login successful"
                            loadUserData()
                            loadNotificationSettingsFromServer()
                            chatHub.connect()
                            withContext(Dispatchers.Main) {
                                onSuccess(role)
                            }
                            com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnSuccessListener { fcmToken ->
                                val prefs = getApplication<Application>()
                                    .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                prefs.edit().putString("fcm_token", fcmToken).apply()

                                sendToken(fcmToken, access)
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                onError("Invalid response")
                            }
                        }
                    } else {
                        val errorMessage = parseJsonField(responseBody, "error") ?: "HTTP ${resp.code}"
                        withContext(Dispatchers.Main) {
                            onError(errorMessage)
                        }
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Unknown error")
                }
            }
        }
    }



    fun register(
        email: String,
        password: String,
        name: String,
        surname: String,
        phone: String,
        role: String,
        carType: String?,
        carColor: String?,
        carNumber: String?,
        cityName: String?,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = if (role == "driver") {
                    """{
                    "email":"$email",
                    "password":"$password",
                    "name":"$name",
                    "surname":"$surname",
                    "role":"$role",
                    "carType":"$carType",
                    "carColor":"$carColor",
                    "carNumber":"$carNumber",
                    "phoneNumber":"$phone",
                    "cityName":"$cityName"
                }"""
                } else {
                    """{
                    "email":"$email",
                    "password":"$password",
                    "name":"$name",
                    "surname":"$surname",
                    "role":"$role",
                    "phoneNumber":"$phone"
                }"""
                }

                val body = json.toRequestBody("application/json".toMediaTypeOrNull())
                val req = Request.Builder()
                    .url("${baseUrl}api/Auth/register")
                    .post(body)
                    .build()

                client.newCall(req).execute().use { resp ->
                    val text = resp.body?.string()

                    if (resp.isSuccessful) {
                        if (!text.isNullOrBlank()) {
                            val access = parseJsonField(text, "accessToken")
                            val refresh = parseJsonField(text, "refreshToken")
                            val roleResponse = parseJsonField(text, "role") ?: role

                            if (!access.isNullOrBlank()) {
                                saveTokens(access, refresh)
                                com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnSuccessListener { fcmToken ->
                                    val prefs = getApplication<Application>()
                                        .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                    prefs.edit().putString("fcm_token", fcmToken).apply()


                                    sendToken(fcmToken, access)
                                }

                                withContext(Dispatchers.Main) {
                                    onSuccess(roleResponse)
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    onError("Invalid response")
                                }
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                onError("Empty response")
                            }
                        }
                    } else {
                        val errorMessage =
                            parseJsonField(text ?: "", "error")
                                ?: "HTTP ${resp.code}"

                        withContext(Dispatchers.Main) {
                            onError(errorMessage)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Error")
                }
            }
        }
    }





    fun createOrder(
        pointA: String,
        pointB: String,
        vehicleType: String,
        date: String,   // "dd/MM/yyyy"
        time: String,   // "HH:mm"
        standard: Boolean = false,
        valuable: Boolean = false,
        fragile: Boolean = false,
        heavy: Boolean = false,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dateParts = date.split("/").map { it.toInt() }
                val timeParts = time.split(":").map { it.toInt() }

                val calendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, dateParts[2])
                    set(Calendar.MONTH, dateParts[1] - 1) // 0-11
                    set(Calendar.DAY_OF_MONTH, dateParts[0])
                    set(Calendar.HOUR_OF_DAY, timeParts[0])
                    set(Calendar.MINUTE, timeParts[1])
                    set(Calendar.SECOND, 0)
                }

                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                val isoDateTime = sdf.format(calendar.time)

                val json = """
            {
                "pointA": "$pointA",
                "pointB": "$pointB",
                "vehicleType": "$vehicleType",
                "dateTime": "$isoDateTime",
                "standard": $standard,
                "valuable": $valuable,
                "fragile": $fragile,
                "heavy": $heavy
            }
            """.trimIndent()

                val body = json.toRequestBody("application/json".toMediaTypeOrNull())
                val req = Request.Builder()
                    .url("${baseUrl}api/Orders/create")
                    .post(body)
                    .addHeader("Authorization", "Bearer ${tokenManager.accessToken}")
                    .build()

                client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        withContext(Dispatchers.Main) {
                            onSuccess()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            onError("HTTP ${resp.code}")
                        }
                    }
                }
            } catch (e: Exception) {
                onError(e.message ?: "Error")
            }
        }


    }

    fun parseRoleFromToken(token: String): String {
        val parts = token.split(".")
        if (parts.size < 2) return "customer"

        val payload = parts[1]
        val decoded = String(
            Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        )

        val match = """"[^"]*role[^"]*"\s*:\s*"([^"]+)"""".toRegex().find(decoded)
        return match?.groups?.get(1)?.value ?: "customer"
    }
    fun getCustomerScheduledOrders(
        onSuccess: (List<Order>) -> Unit,
        onError: (String) -> Unit
    ) {
        val jwtToken = tokenManager.accessToken
        if (jwtToken.isNullOrBlank()) {
            onError("No access token")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val req = Request.Builder()
                    .url("${baseUrl}api/Orders/customerScheduled")
                    .addHeader("Authorization", "Bearer $jwtToken")
                    .get()
                    .build()

                client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val text = resp.body?.string()
                        if (!text.isNullOrBlank()) {
                            val orders = parseOrdersJson(text)
                            withContext(Dispatchers.Main) { onSuccess(orders) }
                        } else {
                            withContext(Dispatchers.Main) { onError("Empty response") }
                        }
                    } else {
                        withContext(Dispatchers.Main) { onError("HTTP ${resp.code}") }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(e.message ?: "Error") }
            }
        }
    }

    fun getCustomerOrdersHistory(
        onSuccess: (List<Order>) -> Unit,
        onError: (String) -> Unit
    ) {
        val jwtToken = tokenManager.accessToken
        if (jwtToken.isNullOrBlank()) {
            onError("No access token")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val req = Request.Builder()
                    .url("${baseUrl}api/Orders/customerHistory")
                    .addHeader("Authorization", "Bearer $jwtToken")
                    .get()
                    .build()

                client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val text = resp.body?.string()
                        if (!text.isNullOrBlank()) {
                            val orders = parseOrdersJson(text)
                            withContext(Dispatchers.Main) { onSuccess(orders) }
                        } else {
                            withContext(Dispatchers.Main) { onError("Empty response") }
                        }
                    } else {
                        withContext(Dispatchers.Main) { onError("HTTP ${resp.code}") }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(e.message ?: "Error") }
            }
        }
    }
    fun getDriverScheduledOrders(
        onSuccess: (List<Order>) -> Unit,
        onError: (String) -> Unit
    ) {
        val jwtToken = tokenManager.accessToken
        if (jwtToken.isNullOrBlank()) {
            onError("No access token")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val req = Request.Builder()
                    .url("${baseUrl}api/Orders/driverScheduled")
                    .addHeader("Authorization", "Bearer $jwtToken")
                    .get()
                    .build()

                client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val text = resp.body?.string()
                        if (!text.isNullOrBlank()) {
                            val orders = parseOrdersJson(text)
                            withContext(Dispatchers.Main) { onSuccess(orders) }
                        } else {
                            withContext(Dispatchers.Main) { onError("Empty response") }
                        }
                    } else {
                        withContext(Dispatchers.Main) { onError("HTTP ${resp.code}") }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(e.message ?: "Error") }
            }
        }
    }

    fun getDriverOrdersHistory(
        onSuccess: (List<Order>) -> Unit,
        onError: (String) -> Unit
    ) {
        val jwtToken = tokenManager.accessToken
        if (jwtToken.isNullOrBlank()) {
            onError("No access token")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val req = Request.Builder()
                    .url("${baseUrl}api/Orders/driverHistory")
                    .addHeader("Authorization", "Bearer $jwtToken")
                    .get()
                    .build()

                client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val text = resp.body?.string()
                        if (!text.isNullOrBlank()) {
                            val orders = parseOrdersJson(text)
                            withContext(Dispatchers.Main) { onSuccess(orders) }
                        } else {
                            withContext(Dispatchers.Main) { onError("Empty response") }
                        }
                    } else {
                        withContext(Dispatchers.Main) { onError("HTTP ${resp.code}") }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(e.message ?: "Error") }
            }
        }
    }
    fun getAvailableOrders(
        onSuccess: (List<Order>) -> Unit,
        onError: (String) -> Unit
    ) {
        val jwtToken = tokenManager.accessToken
        if (jwtToken.isNullOrBlank()) {
            onError("No access token")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val req = Request.Builder()
                    .url("${baseUrl}api/Orders/available")
                    .addHeader("Authorization", "Bearer $jwtToken")
                    .get()
                    .build()

                client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val text = resp.body?.string()
                        if (!text.isNullOrBlank()) {
                            val orders = parseOrdersJson(text)
                            withContext(Dispatchers.Main) { onSuccess(orders) }
                        } else {
                            withContext(Dispatchers.Main) { onError("Empty response") }
                        }
                    } else {
                        withContext(Dispatchers.Main) { onError("HTTP ${resp.code}") }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(e.message ?: "Error") }
            }
        }
    }

    fun getOrderById(
        orderId: Int,
        onSuccess: (Order) -> Unit,
        onError: (String) -> Unit
    ) {
        val jwtToken = tokenManager.accessToken
        if (jwtToken.isNullOrBlank()) {
            onError("No access token")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val req = Request.Builder()
                    .url("${baseUrl}api/Orders/$orderId")
                    .addHeader("Authorization", "Bearer $jwtToken")
                    .get()
                    .build()

                client.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) {
                        withContext(Dispatchers.Main) { onError("HTTP ${resp.code}") }
                        return@use
                    }

                    val body = resp.body?.string()
                    if (body.isNullOrBlank()) {
                        withContext(Dispatchers.Main) { onError("Empty response") }
                        return@use
                    }

                    val obj = org.json.JSONObject(body)
                    val sdfInput = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    sdfInput.timeZone = java.util.TimeZone.getTimeZone("UTC")
                    val sdfOutput = java.text.SimpleDateFormat("dd/MM/yyyy")

                    val orderNumber = obj.getInt("orderId")
                    val dateTimeStr = obj.getString("dateTime")
                    val dateObj = sdfInput.parse(dateTimeStr)
                    val date = sdfOutput.format(dateObj)
                    val time = java.text.SimpleDateFormat("HH:mm").format(dateObj)
                    val from = obj.getString("pointA")
                    val to = obj.getString("pointB")
                    val vehicleType = obj.getString("vehicleType")
                    val statusInt = obj.getInt("status")
                    val status = when(statusInt) {
                        0 -> "Pending"
                        1 -> "Accepted"
                        2 -> "InProgress"
                        3 -> "Completed"
                        4 -> "Canceled"
                        5 -> "Driver Coming"
                        6 -> "Picking up"
                        7 -> "Delivering"
                        else -> "Unknown"
                    }

                    val cargoOptions = mutableListOf<String>()
                    if(obj.getBoolean("standard")) cargoOptions.add("Standard")
                    if (obj.getBoolean("valuable")) cargoOptions.add("Valuable")
                    if (obj.getBoolean("fragile")) cargoOptions.add("Fragile")
                    if (obj.getBoolean("heavy")) cargoOptions.add("Heavy")


                    val driverId = if (obj.has("driverId") && !obj.isNull("driverId")) obj.getInt("driverId") else null
                    val customerId = if (obj.has("customerId") && !obj.isNull("customerId")) obj.getInt("customerId") else null
                    val price = obj.getDouble("price")

                    val order = Order(
                        orderNumber = orderNumber,
                        date = date,
                        time = time,
                        from = from,
                        to = to,
                        distance = obj.optDouble("distance", 0.0),
                        vehicleType = vehicleType,
                        cargoOptions = cargoOptions,
                        status = status,
                        driverId = driverId,
                        customerId = customerId,
                        price = price
                    )

                    withContext(Dispatchers.Main) { onSuccess(order) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(e.message ?: "Error") }
            }
        }
    }


    fun acceptOrder(
        orderId: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val jwtToken = tokenManager.accessToken
        if (jwtToken.isNullOrBlank()) {
            onError("No access token")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = """{"orderId":"$orderId"}"""
                val body = json.toRequestBody("application/json".toMediaTypeOrNull())
                val req = Request.Builder()
                    .url("${baseUrl}api/Orders/accept")
                    .post(body)
                    .addHeader("Authorization", "Bearer $jwtToken")
                    .build()

                client.newCall(req).execute().use { resp ->
                    withContext(Dispatchers.Main) {
                        if (resp.isSuccessful) {
                            onSuccess()
                        } else {
                            onError("HTTP ${resp.code}")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(e.message ?: "Error") }
            }
        }
    }


    private fun parseOrdersJson(json: String): List<Order> {
        val ordersList = mutableListOf<Order>()
        val jsonArray = org.json.JSONArray(json)

        val sdfInput = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        sdfInput.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val sdfOutput = java.text.SimpleDateFormat("dd/MM/yyyy")

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val orderNumber = obj.getInt("orderId")
            val dateTimeStr = obj.getString("dateTime")
            val dateObj = sdfInput.parse(dateTimeStr)
            val distance = obj.getDouble("distance")
            val date = sdfOutput.format(dateObj)
            val time = java.text.SimpleDateFormat("HH:mm").format(dateObj)
            val from = obj.getString("pointA")
            val to = obj.getString("pointB")
            val vehicleType = obj.getString("vehicleType")
            val statusInt = obj.getInt("status")
            val status = when(statusInt) {
                0 -> "Pending"
                1 -> "Accepted"
                2 -> "InProgress"
                3 -> "Completed"
                4 -> "Canceled"
                5 -> "Driver Coming"
                6 -> "Picking up"
                7 -> "Delivering"
                else -> "Unknown"
            }

            val cargoOptions = mutableListOf<String>()
            if(obj.getBoolean("standard")) cargoOptions.add("Standard")
            if (obj.getBoolean("valuable")) cargoOptions.add("Valuable")
            if (obj.getBoolean("fragile")) cargoOptions.add("Fragile")
            if (obj.getBoolean("heavy")) cargoOptions.add("Heavy")


            val driverId = if (obj.has("driverId") && !obj.isNull("driverId")) obj.getInt("driverId") else null
            val customerId = if (obj.has("customerId") && !obj.isNull("customerId")) obj.getInt("customerId") else null
            val price = obj.getDouble("price")

            ordersList.add(
                Order(
                    orderNumber = orderNumber,
                    date = date,
                    time = time,
                    from = from,
                    to = to,
                    distance = distance,
                    vehicleType = vehicleType,
                    cargoOptions = cargoOptions,
                    status = status,
                    driverId = driverId,
                    customerId = customerId,
                    price = price
                )
            )
        }

        return ordersList
    }


    fun editOrder(
        orderId: Int,
        pointA: String,
        pointB: String,
        vehicleType: String,
        date: String,   // "dd/MM/yyyy"
        time: String,   // "HH:mm"
        standard: Boolean = false,
        valuable: Boolean = false,
        fragile: Boolean = false,
        heavy: Boolean = false,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dateParts = date.split("/").map { it.toInt() }
                val timeParts = time.split(":").map { it.toInt() }

                val calendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, dateParts[2])
                    set(Calendar.MONTH, dateParts[1] - 1)
                    set(Calendar.DAY_OF_MONTH, dateParts[0])
                    set(Calendar.HOUR_OF_DAY, timeParts[0])
                    set(Calendar.MINUTE, timeParts[1])
                    set(Calendar.SECOND, 0)
                }

                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                val isoDateTime = sdf.format(calendar.time)

                val json = """
                {
                    "orderId": $orderId,
                    "pointA": "$pointA",
                    "pointB": "$pointB",
                    "vehicleType": "$vehicleType",
                    "dateTime": "$isoDateTime",
                    "standard": $standard,
                    "valuable": $valuable,
                    "fragile": $fragile,
                    "heavy": $heavy
                }
            """.trimIndent()

                val body = json.toRequestBody("application/json".toMediaTypeOrNull())
                val req = Request.Builder()
                    .url("${baseUrl}api/Orders/edit")
                    .post(body)
                    .addHeader("Authorization", "Bearer ${tokenManager.accessToken}")
                    .build()

                client.newCall(req).execute().use { resp ->
                    withContext(Dispatchers.Main) {
                        if (resp.isSuccessful) onSuccess()
                        else onError("HTTP ${resp.code}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(e.message ?: "Error") }
            }
        }
    }

    fun cancelOrder(
        orderId: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val jwtToken = tokenManager.accessToken
        if (jwtToken.isNullOrBlank()) { onError("No access token"); return }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = """{"orderId":$orderId}"""
                val body = json.toRequestBody("application/json".toMediaTypeOrNull())
                val req = Request.Builder()
                    .url("${baseUrl}api/Orders/cancel")
                    .post(body)
                    .addHeader("Authorization", "Bearer $jwtToken")
                    .build()

                client.newCall(req).execute().use { resp ->
                    withContext(Dispatchers.Main) {
                        if (resp.isSuccessful) onSuccess()
                        else onError("HTTP ${resp.code}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(e.message ?: "Error") }
            }
        }
    }

    fun loadUserData() {
        val jwt = tokenManager.accessToken ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val req = Request.Builder()
                    .url("${baseUrl}api/Auth/userinfo")
                    .addHeader("Authorization", "Bearer $jwt")
                    .get()
                    .build()

                client.newCall(req).execute().use { resp ->
                    val body = resp.body?.string()

                    if (!resp.isSuccessful || body.isNullOrBlank()) return@use

                    val json = org.json.JSONObject(body)

                    val user = UserData(
                        name = json.getString("name"),
                        surname = json.getString("surname"),
                        email = json.getString("email"),
                        phoneNumber = json.getString("phoneNumber"),
                        role = json.getString("role"),
                        carType = json.optString("carType", null),
                        carColor = json.optString("carColor", null),
                        carNumber = json.optString("carNumber", null),
                        cityName = json.optString("cityName", null),
                        id = json.getInt("id")

                    )

                    _userData.value = user
                }
            } catch (_: Exception) {}
        }
    }


    fun updateProfile(
        name: String,
        surname: String,
        email: String,
        phone: String,
        carType: String?,
        carColor: String?,
        carNumber: String?,
        cityName: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val jwt = tokenManager.accessToken ?: return onError("No token")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = """
                {
                    "email": "$email",
                    "name": "$name",
                    "surname": "$surname",
                    "phoneNumber": "$phone",
                    "carType": ${if (carType == null) "null" else "\"$carType\""},
                    "carColor": ${if (carColor == null) "null" else "\"$carColor\""},
                    "carNumber": ${if (carNumber == null) "null" else "\"$carNumber\""},
                    "cityName": ${if (cityName == null) "null" else "\"$cityName\""}
                }
            """.trimIndent()

                val body = json.toRequestBody("application/json".toMediaTypeOrNull())
                val req = Request.Builder()
                    .url("${baseUrl}api/Auth/updateProfile")
                    .addHeader("Authorization", "Bearer $jwt")
                    .post(body)
                    .build()

                client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        loadUserData()
                        withContext(Dispatchers.Main) { onSuccess() }
                    } else {
                        withContext(Dispatchers.Main) { onError("HTTP ${resp.code}") }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(e.message ?: "Error") }
            }
        }
    }

    fun deleteAccount(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val jwtToken = tokenManager.accessToken
        if (jwtToken.isNullOrBlank()) {
            onError("No access token")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val req = Request.Builder()
                    .url("${baseUrl}api/Auth/deleteAccount")
                    .delete()
                    .addHeader("Authorization", "Bearer $jwtToken")
                    .build()

                client.newCall(req).execute().use { resp ->
                    withContext(Dispatchers.Main) {
                        if (resp.isSuccessful) {
                            clearTokens()
                            _userData.value = null
                            status.value = "Account deleted"
                            onSuccess()
                        } else {
                            onError("HTTP ${resp.code}")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(e.message ?: "Error") }
            }
        }
    }

    fun getChats(
        onSuccess: (List<Chat>) -> Unit,
        onError: (String) -> Unit
    ) {
        val jwtToken = tokenManager.accessToken ?: run { onError("No access token"); return }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val req = Request.Builder()
                    .url("${baseUrl}api/chat/list")
                    .addHeader("Authorization", "Bearer $jwtToken")
                    .get()
                    .build()

                client.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) { withContext(Dispatchers.Main) { onError("HTTP ${resp.code}") }; return@use }
                    val body = resp.body?.string() ?: run { withContext(Dispatchers.Main) { onError("Empty response") }; return@use }
                    val chats = Json.decodeFromString<List<Chat>>(body)
                    withContext(Dispatchers.Main) { onSuccess(chats) }
                }
            } catch (e: Exception) { withContext(Dispatchers.Main) { onError(e.message ?: "Error") } }
        }
    }


    fun loadChat(orderId: Int, onError: (String) -> Unit) {
        val jwt = tokenManager.accessToken ?: return onError("No access token")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val req = Request.Builder()
                    .url("${baseUrl}api/chat/show/$orderId")
                    .addHeader("Authorization", "Bearer $jwt")
                    .get()
                    .build()

                client.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) {
                        withContext(Dispatchers.Main) { onError("HTTP ${resp.code}") }
                        return@use
                    }
                    val body = resp.body?.string() ?: run {
                        withContext(Dispatchers.Main) { onError("Empty response") }
                        return@use
                    }

                    val chatResp = Json.decodeFromString<Chat>(body)
                    withContext(Dispatchers.Main) {
                        _currentChat.value = chatResp
                        _currentChatId.value = chatResp.chatId
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(e.message ?: "Error") }
            }
        }
    }

    fun loadChatHistory(chatId: Int) {
        val jwt = tokenManager.accessToken ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val req = Request.Builder()
                    .url("${baseUrl}api/chat/history/$chatId")
                    .addHeader("Authorization", "Bearer $jwt")
                    .get()
                    .build()

                client.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) return@use
                    val body = resp.body?.string() ?: return@use
                    val msgsFromServer = Json.decodeFromString<List<Message>>(body)
                        .map { it.copy(chatId = chatId) }
                        .sortedBy { it.sentAt }

                    withContext(Dispatchers.Main) {
                        _chatMessages.value = msgsFromServer
                    }
                }
            } catch (_: Exception) {}
        }
    }
    fun sendMessage(
        text: String,
        orderId: Int,
        onError: (String) -> Unit
    ) {
        val jwtToken = tokenManager.accessToken
            ?: return onError("No access token")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = SendMessageRequest(
                    orderId = orderId,
                    text = text
                )

                val json = Json.encodeToString(request)
                val body = json.toRequestBody("application/json".toMediaTypeOrNull())

                val req = Request.Builder()
                    .url("${baseUrl}api/chat/send")
                    .post(body)
                    .addHeader("Authorization", "Bearer $jwtToken")
                    .build()

                client.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) {
                        onError("HTTP ${resp.code}")
                    }
                }
            } catch (e: Exception) {
                onError(e.message ?: "Error")
            }
        }
    }

    suspend fun getUserDataById(userId: Int): UserData? =
        withContext(Dispatchers.IO) {
            val jwt = tokenManager.accessToken ?: return@withContext null

            try {
                val req = Request.Builder()
                    .url("${baseUrl}api/Auth/userinfo/$userId")
                    .addHeader("Authorization", "Bearer $jwt")
                    .get()
                    .build()

                client.newCall(req).execute().use { resp ->
                    val body = resp.body?.string() ?: return@withContext null
                    if (!resp.isSuccessful) return@withContext null

                    val json = org.json.JSONObject(body)

                    UserData(
                        id = json.getInt("id"),
                        name = json.optString("name"),
                        surname = json.optString("surname"),
                        email = json.optString("email"),
                        phoneNumber = json.optString("phoneNumber"),
                        role = json.optString("role"),
                        carType = json.optString("carType", null),
                        carColor = json.optString("carColor", null),
                        carNumber = json.optString("carNumber", null),
                        cityName = json.optString("cityName", null)
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }



    private fun observeIncomingMessages() {
        viewModelScope.launch {
            chatHub.incomingMessages.collect { msg ->
                _currentChatId.value?.let { currentId ->
                    if (msg.chatId == currentId) {
                        _chatMessages.update { old ->
                            if (old.any { it.id == msg.id }) old
                            else old + Message(
                                id = msg.id,
                                chatId = msg.chatId,
                                senderId = msg.senderId,
                                text = msg.text,
                                sentAt = msg.sentAt
                            )
                        }
                    }
                }
            }
        }
    }



    init {
        observeIncomingMessages()
        viewModelScope.launch {
            loadNotificationSettingsFromServer()
        }

    }


    fun tryAutoLogin(
        onSuccess: (String) -> Unit,
        onFailure: () -> Unit
    ) {
        val access = tokenManager.accessToken
        val refresh = tokenManager.refreshToken

        if (refresh.isNullOrBlank()) {
            onFailure()
            return
        }

        if (!access.isNullOrBlank()) {
            viewModelScope.launch {
                loadUserData()
                val role = parseRoleFromToken(access)
                chatHub.connect()
                onSuccess(role)
            }
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnSuccessListener { fcmToken ->
                val prefs = getApplication<Application>()
                    .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                prefs.edit().putString("fcm_token", fcmToken).apply()

                sendToken(fcmToken, access)
            }
            return
        }

        refreshToken(
            onSuccess = { newAccess ->
                viewModelScope.launch {
                    loadUserData()
                    val role = parseRoleFromToken(newAccess)
                    loadNotificationSettingsFromServer()
                    chatHub.connect()
                    onSuccess(role)
                }
            },
            onError = {
                clearTokens()
                onFailure()
            }
        )
    }

    private fun sendToken(token: String, jwtToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = Json.encodeToString(FcmTokenDto(token))
                val body = json.toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url("${baseUrl}api/push/token")
                    .addHeader("Authorization", "Bearer $jwtToken")
                    .post(body)
                    .build()

                OkHttpClient().newCall(request).execute().close()
            } catch (e: Exception) {
            }
        }
    }


    fun enterChat(chatId: Int) {
        chatHub.enterChat(chatId)
    }

    fun leaveChat() {
        chatHub.leaveChat()
    }

    fun updateNotificationSettings(settings: NotificationSettings) {
        _notificationSettings.value = settings

        viewModelScope.launch {
            println(settings.toString())
            sendNotificationSettingsToServer(settings)
        }
    }


    suspend fun loadNotificationSettingsFromServer() {
        val jwt = tokenManager.accessToken ?: return

        withContext(Dispatchers.IO) {
            try {
                val req = Request.Builder()
                    .url("${baseUrl}api/notifications/settings")
                    .addHeader("Authorization", "Bearer $jwt")
                    .get()
                    .build()

                val res = client.newCall(req).execute()
                val json = res.body?.string()
                println(json)
                val settings = if (!json.isNullOrBlank()) {
                    Json.decodeFromString<NotificationSettings>(json)
                } else {
                    NotificationSettings()
                }

                    _notificationSettings.value = settings
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }



    private suspend fun sendNotificationSettingsToServer(settings: NotificationSettings){
        val jwt = tokenManager.accessToken ?: return

        try {
            val json = Json { encodeDefaults = true }.encodeToString(settings)
            val body = json.toRequestBody("application/json".toMediaType())

            val req = Request.Builder()
                .url("${baseUrl}api/notifications/settings")
                .addHeader("Authorization", "Bearer $jwt")
                .post(body)
                .build()

            println(json)
            withContext(Dispatchers.IO) {
                client.newCall(req).execute().use { response ->
                    if(!response.isSuccessful)
                        println("Failed to send notification settings to server ${response.code}")
                }
            }
        } catch (e: Exception){
            e.printStackTrace()
        }
    }



    fun confirmEmail(
        code: String,
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = """
                {
                    "email": "$email",
                    "code": "$code"
                }
            """.trimIndent()

                val body = json.toRequestBody("application/json".toMediaTypeOrNull())

                val req = Request.Builder()
                    .url("${baseUrl}api/Auth/verifyEmail")
                    .post(body)
                    .build()

                client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        withContext(Dispatchers.Main) {
                            onSuccess()
                        }
                    } else {
                        val text = resp.body?.string()
                        val error =
                            parseJsonField(text ?: "", "error")
                                ?: "Invalid code"

                        withContext(Dispatchers.Main) {
                            onError(error)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Error")
                }
            }
        }
    }


    fun resendEmail(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = """
                {
                    "email": "$email"
                }
            """.trimIndent()

                val body = json.toRequestBody("application/json".toMediaTypeOrNull())

                val req = Request.Builder()
                    .url("${baseUrl}api/Auth/resendEmailCode")
                    .post(body)
                    .build()

                client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        withContext(Dispatchers.Main) {
                            onSuccess()
                        }
                    } else {
                        val text = resp.body?.string()
                        val error =
                            parseJsonField(text ?: "", "error")
                                ?: "Failed to send code"

                        withContext(Dispatchers.Main) {
                            onError(error)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Error")
                }
            }
        }
    }

    fun sendPasswordReset(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = """
                {
                    "email": "$email"
                }
            """.trimIndent()

                val body = json.toRequestBody("application/json".toMediaTypeOrNull())

                val req = Request.Builder()
                    .url("${baseUrl}api/Auth/requestPasswordReset")
                    .post(body)
                    .build()

                client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        withContext(Dispatchers.Main) {
                            onSuccess()
                        }
                    } else {
                        val text = resp.body?.string()
                        val error =
                            parseJsonField(text ?: "", "error")
                                ?: "Failed to send reset code"

                        withContext(Dispatchers.Main) {
                            onError(error)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Error")
                }
            }
        }
    }

    fun changePassword(
        email: String,
        code: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = """
                {
                    "email": "$email",
                    "code": "$code",
                    "newPassword": "$newPassword"
                }
            """.trimIndent()

                val body = json.toRequestBody("application/json".toMediaTypeOrNull())

                val req = Request.Builder()
                    .url("${baseUrl}api/Auth/resetPassword")
                    .post(body)
                    .build()

                client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        withContext(Dispatchers.Main) {
                            onSuccess()
                        }
                    } else {
                        val text = resp.body?.string()
                        val error =
                            parseJsonField(text ?: "", "error")
                                ?: "Failed to reset password"

                        withContext(Dispatchers.Main) {
                            onError(error)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Error")
                }
            }
        }
    }

    fun comingOrder(
        orderId: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val jwtToken = tokenManager.accessToken
        if (jwtToken.isNullOrBlank()) {
            onError("No access token")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = """{"orderId":"$orderId"}"""
                val body = json.toRequestBody("application/json".toMediaTypeOrNull())
                val req = Request.Builder()
                    .url("${baseUrl}api/Orders/coming")
                    .post(body)
                    .addHeader("Authorization", "Bearer $jwtToken")
                    .build()

                client.newCall(req).execute().use { resp ->
                    withContext(Dispatchers.Main) {
                        if (resp.isSuccessful) {
                            onSuccess()
                        } else {
                            onError("HTTP ${resp.code}")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(e.message ?: "Error") }
            }
        }
    }



    fun pickingOrder(
        orderId: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val jwtToken = tokenManager.accessToken
        if (jwtToken.isNullOrBlank()) {
            onError("No access token")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = """{"orderId":"$orderId"}"""
                val body = json.toRequestBody("application/json".toMediaTypeOrNull())
                val req = Request.Builder()
                    .url("${baseUrl}api/Orders/picking")
                    .post(body)
                    .addHeader("Authorization", "Bearer $jwtToken")
                    .build()

                client.newCall(req).execute().use { resp ->
                    withContext(Dispatchers.Main) {
                        if (resp.isSuccessful) {
                            onSuccess()
                        } else {
                            onError("HTTP ${resp.code}")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(e.message ?: "Error") }
            }
        }
    }
    fun deliveringOrder(
        orderId: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val jwtToken = tokenManager.accessToken
        if (jwtToken.isNullOrBlank()) {
            onError("No access token")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = """{"orderId":"$orderId"}"""
                val body = json.toRequestBody("application/json".toMediaTypeOrNull())
                val req = Request.Builder()
                    .url("${baseUrl}api/Orders/delivering")
                    .post(body)
                    .addHeader("Authorization", "Bearer $jwtToken")
                    .build()

                client.newCall(req).execute().use { resp ->
                    withContext(Dispatchers.Main) {
                        if (resp.isSuccessful) {
                            onSuccess()
                        } else {
                            onError("HTTP ${resp.code}")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(e.message ?: "Error") }
            }
        }
    }


    fun completeOrder(
        orderId: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val jwtToken = tokenManager.accessToken
        if (jwtToken.isNullOrBlank()) {
            onError("No access token")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = """{"orderId":"$orderId"}"""
                val body = json.toRequestBody("application/json".toMediaTypeOrNull())
                val req = Request.Builder()
                    .url("${baseUrl}api/Orders/completed")
                    .post(body)
                    .addHeader("Authorization", "Bearer $jwtToken")
                    .build()

                client.newCall(req).execute().use { resp ->
                    withContext(Dispatchers.Main) {
                        if (resp.isSuccessful) {
                            onSuccess()
                        } else {
                            onError("HTTP ${resp.code}")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(e.message ?: "Error") }
            }
        }
    }





}




