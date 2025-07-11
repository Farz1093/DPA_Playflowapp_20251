package com.esan.payflowapp.core.utils

import java.util.Locale

fun Double.toTwoDecimal(): String {
    val format = java.text.NumberFormat.getNumberInstance(Locale.US)
    format.minimumFractionDigits = 2
    format.maximumFractionDigits = 2
    return format.format(this)
}

fun Long.toReadableDate(): String {
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "PE"))
    return sdf.format(java.util.Date(this))
}