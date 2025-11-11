package com.eric.proyectoqr.network

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email")    val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("device")   val device: String
)