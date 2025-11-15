package com.eric.proyectoqr

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
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

        // Mostrar el QR crudo
        binding.qrDataTextView.text = "QR: $qrRaw"

        binding.btnClose.setOnClickListener { finish() }

        if (token.isBlank()) {
            binding.tvMessage.text = "Mensaje: Token vacío, vuelve a escanear."
            return
        }

        // Llamar a la API para validar el boleto
        lifecycleScope.launch {
            try {
                // 1) Obtener la instancia de ApiService usando el contexto de la Activity
                val api = RetrofitClient.getInstance(this@TicketInfoActivity)

                // 2) Llamar al endpoint
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
                    Log.e(
                        "TicketInfoActivity",
                        "Error API $code: ${response.errorBody()?.string()}"
                    )
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

        // Texto base
        binding.qrDataTextView.text = "QR: $qrRaw"
        binding.statusChip.text = if (status.isBlank()) "-" else status.uppercase()

        binding.tvEventName.text = "Evento: $eventName"
        binding.tvDate.text = "Fecha: $date"
        binding.tvShift.text = "Turno: $shift"
        binding.tvMesa.text = "Mesa: $mesa"
        binding.tvTicketId.text = "Ticket ID: $ticketId"
        binding.tvReservationId.text = "Reservación ID: $reservationId"
        binding.tvUsedAt.text = "Usado en: $usedAt"
        binding.tvMessage.text = "Mensaje: $message"

        // Colores del chip según status
        val isOk = status.equals("ok", true) ||
                status.equals("válido", true) ||
                status.equals("valido", true) ||
                status.equals("valid", true)

        val bgColorRes = if (isOk) R.color.teal_200 else android.R.color.holo_red_dark
        val textColor  = if (isOk) Color.BLACK else Color.WHITE

        val chip = binding.statusChip as Chip
        val bgColor = ContextCompat.getColor(this, bgColorRes)
        chip.chipBackgroundColor = ColorStateList.valueOf(bgColor)
        chip.setTextColor(textColor)
    }
}
