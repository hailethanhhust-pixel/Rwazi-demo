package com.example.rwazihomework.ui.home

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.core.graphics.toColorInt
import java.text.DateFormat
import java.util.Date

fun formatDateStamp(timestamp: Long): String {
    return DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(timestamp))
}

fun colorFromHex(hex: String): Color {
    return runCatching { Color(hex.toColorInt()) }.getOrElse { Color(0xFFE0E0E0) }
}

fun readableTextColor(background: Color): Color {
    return if (background.luminance() > 0.55f) Color.Black else Color.White
}
