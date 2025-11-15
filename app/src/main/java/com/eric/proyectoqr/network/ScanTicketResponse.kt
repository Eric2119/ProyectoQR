package com.eric.proyectoqr.network

import com.google.gson.annotations.SerializedName

data class ScanTicketResponse(
    val changed: Boolean? = null,
    val status: String? = null,

    @SerializedName("id_mesa")
    val idMesa: Int? = null,

    @SerializedName("used_at")
    val usedAt: String? = null,

    @SerializedName("ticket_id")
    val ticketId: Int? = null,

    @SerializedName("reservation_id")
    val reservationId: Int? = null,

    @SerializedName("event_name")
    val eventName: String? = null,

    val date: String? = null,
    val shift: String? = null,
    val message: String? = null
)
