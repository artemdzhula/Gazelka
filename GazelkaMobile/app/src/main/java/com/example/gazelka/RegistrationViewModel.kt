package com.example.gazelka
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue


class RegistrationViewModel : ViewModel() {

    var email: String = ""
    var password: String = ""
    var name: String = ""
    var surname: String = ""
    var role: String? = null
    var carType: String? = null
    var carColor: String? = null
    var carNumber: String? = null
    var phone: String = ""
    var cityName by mutableStateOf("")
}