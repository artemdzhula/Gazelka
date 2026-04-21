package com.example.gazelka.models

data class UserData(
    val name: String,
    val surname: String,
    val email: String,
    val phoneNumber: String,
    val role: String,
    val carType: String?,
    val carColor: String?,
    val carNumber: String?,
    val id: Int,
    val cityName: String?,
)

