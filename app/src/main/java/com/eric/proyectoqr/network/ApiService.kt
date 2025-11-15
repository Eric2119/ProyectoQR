package com.eric.proyectoqr.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    // Login que ya tienes
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    // ✅ Validador de boletos
    @POST("validator/tickets/scan")
    suspend fun validateTicket(
        @Body body: ScanTicketRequest
    ): Response<ScanTicketResponse>
}
