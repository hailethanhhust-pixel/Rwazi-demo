package com.example.rwazihomework.domain.model

import kotlin.random.Random

private val NOTE_COLOR_PALETTE = listOf(
    "#F28B82", // red
    "#FBBC04", // orange
    "#FFF475", // yellow
    "#CCFF90", // green
    "#A7FFEB", // teal
    "#CBF0F8", // cyan
    "#AECBFA", // blue
    "#D7AEFB", // purple
    "#FDCFE8", // pink
    "#E6C9A8" // brown
)

fun nextNoteColorHex(usedColors: Set<String>): String {
    val available = NOTE_COLOR_PALETTE.filterNot { usedColors.contains(it) }
    if (available.isNotEmpty()) {
        return available.random()
    }

    return NOTE_COLOR_PALETTE.random()
}