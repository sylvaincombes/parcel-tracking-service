package com.shoplane.parceltracking.presentation.backoffice

import com.shoplane.parceltracking.domain.parcel.Parcel

object ParcelCsv {
    private val HEADER = listOf("tracking_number", "carrier", "order_reference", "status", "registered_at", "delivered_at")
    private const val LINE_SEPARATOR = "\r\n"
    private val FORMULA_PREFIXES = setOf('=', '+', '-', '@', '\t', '\r')
    private val CHARACTERS_REQUIRING_QUOTES = setOf(',', '"', '\n', '\r')

    fun write(parcels: List<Parcel>): String =
        (listOf(HEADER) + parcels.map(::toRecord))
            .joinToString(separator = LINE_SEPARATOR, postfix = LINE_SEPARATOR) { record ->
                record.joinToString(separator = ",", transform = ::toField)
            }

    private fun toRecord(parcel: Parcel): List<String> =
        listOf(
            parcel.trackingNumber.value,
            parcel.carrierCode.value,
            parcel.orderReference.value,
            parcel.status.name,
            parcel.registeredAt.toString(),
            parcel.deliveredAt?.toString().orEmpty(),
        )

    private fun toField(value: String): String = quotedIfNeeded(withoutFormula(value))

    private fun withoutFormula(value: String): String =
        if (value.firstOrNull() in FORMULA_PREFIXES) "'$value" else value

    private fun quotedIfNeeded(value: String): String =
        if (value.any { it in CHARACTERS_REQUIRING_QUOTES }) "\"${value.replace("\"", "\"\"")}\"" else value
}
