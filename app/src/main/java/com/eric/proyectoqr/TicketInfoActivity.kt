package com.eric.proyectoqr

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.eric.proyectoqr.databinding.ActivityTicketInfoBinding
import com.eric.proyectoqr.network.RetrofitClient
import com.eric.proyectoqr.network.ScanTicketRequest
import com.eric.proyectoqr.network.ScanTicketResponse
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class TicketInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTicketInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTicketInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Lo que viene del escáner
        val qrRaw = intent.getStringExtra("qr_raw") ?: "-"
        val token = intent.getStringExtra("token") ?: ""

        // Mostrar QR crudo oculto
        binding.qrDataTextView.text = "QR: $qrRaw"

        binding.btnClose.setOnClickListener { finish() }

        if (token.isBlank()) {
            binding.tvMessage.text = "Mensaje: Token vacío, vuelve a escanear."
            return
        }

        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getInstance(this@TicketInfoActivity)

                val response = api.validateTicket(
                    ScanTicketRequest(token = token)
                )

                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        bindTicketInfo(qrRaw, data)
                    } else {
                        binding.tvMessage.text = "Mensaje: respuesta vacía del servidor."
                    }
                } else {
                    val code = response.code()
                    binding.tvMessage.text = "Mensaje: error $code al validar el boleto."
                    Log.e("TicketInfoActivity", "Error API $code: ${response.errorBody()?.string()}")
                }

            } catch (e: Exception) {
                Log.e("TicketInfoActivity", "Error llamando API", e)
                binding.tvMessage.text =
                    "Mensaje: error de red: ${e.localizedMessage ?: "desconocido"}"
            }
        }
    }

    private fun bindTicketInfo(qrRaw: String, data: ScanTicketResponse) {

        val status        = data.status ?: "-"
        val eventName     = data.eventName ?: "-"
        val date          = data.date ?: "-"
        val shift         = data.shift ?: "-"
        val mesa          = data.idMesa?.toString() ?: "-"
        val ticketId      = data.ticketId?.toString() ?: "-"
        val reservationId = data.reservationId?.toString() ?: "-"
        val usedAt        = data.usedAt ?: "-"
        val message       = data.message ?: "-"

        // ---------------- TEXTO BASE ----------------
        binding.qrDataTextView.text = "QR: $qrRaw"
        binding.statusChip.text = status.uppercase()

        binding.tvEventName.text = "Evento: $eventName"
        binding.tvDate.text = "Fecha: $date"
        binding.tvShift.text = "Turno: $shift"
        binding.tvMesa.text = "Mesa: $mesa"
        binding.tvTicketId.text = "Ticket ID: $ticketId"
        binding.tvReservationId.text = "Reservación ID: $reservationId"
        binding.tvUsedAt.text = "Usado en: $usedAt"
        binding.tvMessage.text = "Mensaje: $message"

        // ---------------- LÓGICA REAL DE ESTADOS ----------------
        val msg = message.lowercase()

        val isPrimeraVez =
            msg.contains("marcado como usado") ||
                    (msg.contains("validado") && !msg.contains("ya fue"))

        val isSegundaVez =
            msg.contains("ya fue usado") ||
                    msg.contains("ya usado") ||
                    msg.contains("ya fue escaneado")

        // Colores
        val colorSuccess = ContextCompat.getColor(this, R.color.green_success)
        val colorError   = ContextCompat.getColor(this, R.color.red_error)

        val chip = binding.statusChip as Chip
        val icon = binding.statusIcon  // nuevo ícono

        when {
            // ---------------- PRIMER ESCANEO (VERDE) ----------------
            isPrimeraVez -> {
                chip.chipBackgroundColor = ColorStateList.valueOf(colorSuccess)
                chip.setTextColor(Color.BLACK)

                binding.tvMessage.setTextColor(colorSuccess)

                icon.setImageResource(R.drawable.ic_success)
                icon.setColorFilter(colorSuccess)
                icon.visibility = View.VISIBLE
            }

            // ---------------- SEGUNDO ESCANEO / YA USADO (ROJO) ----------------
            isSegundaVez -> {
                chip.chipBackgroundColor = ColorStateList.valueOf(colorError)
                chip.setTextColor(Color.WHITE)

                binding.tvMessage.setTextColor(colorError)

                icon.setImageResource(R.drawable.ic_error)
                icon.setColorFilter(colorError)
                icon.visibility = View.VISIBLE
            }

            else -> {
                // Estado raro / fallback
                val neutral = ContextCompat.getColor(this, R.color.loginLabel)

                chip.chipBackgroundColor = ColorStateList.valueOf(neutral)
                chip.setTextColor(Color.WHITE)

                binding.tvMessage.setTextColor(neutral)

                icon.visibility = View.GONE
            }
        }
    }
}
