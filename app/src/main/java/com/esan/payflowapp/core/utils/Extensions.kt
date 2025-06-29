package com.esan.payflowapp.core.utils

import java.util.Locale

fun Double.toTwoDecimal(): String {
    return String.format(Locale("es", "PE"), "%.2f", this)
}