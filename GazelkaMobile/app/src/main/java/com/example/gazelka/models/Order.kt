package com.example.gazelka.models

import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val orderNumber: Int,
    val date: String,
    val time: String,
    val from: String,
    val to: String,
    val distance: Double?,
    val vehicleType: String,
    val cargoOptions: List<String>,
    val status: String,
    val driverId: Int? = null,
    val customerId: Int? = null,
    val price: Double?
)