package com.eric.proyectoqr.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    /**
     * Emulador:    "http://10.0.2.2:8000/api/"
     * Dispositivo: "http://IP_DE_TU_PC:8000/api/"
     * Cloudflared: "https://<tu-subdominio>.trycloudflare.com/api/"
     */
    private const val BASE_URL = "https://desired-students-expanding-writes.trycloudflare.com/api/"

    fun getInstance(context: Context): ApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Si tu endpoint requiere Bearer, conserva tu AuthInterceptor
        val http = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context)) // si no usas token, quita esta línea
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}
