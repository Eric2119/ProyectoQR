package com.eric.proyectoqr.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    /**
     * Solicita la autenticación. El backend espera un JSON con email, password y device.
     */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}
