package com.example.rwazihomework.data.repository

import com.example.rwazihomework.domain.model.Note
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

internal object LegacyNotesParser {
    fun parse(
        raw: String?,
        nowMillis: Long = System.currentTimeMillis(),
        nextColor: (Set<String>) -> String
    ): List<Note> {
        if (raw.isNullOrBlank()) return emptyList()

        return runCatching {
            val jsonArray = JSONArray(raw)
            val migrationBaseTime = nowMillis - (jsonArray.length() * 1_000L)
            val usedColors = mutableSetOf<String>()
            List(jsonArray.length()) { index ->
                when (val value = jsonArray.get(index)) {
                    is JSONObject -> {
                        val text = value.optString("text", "")
                        val createdAt = value.optLong("createdAt", migrationBaseTime + index * 1_000L)
                        val id = value.optString("id", UUID.randomUUID().toString())
                        val color = value.optString("backgroundColorHex", "")
                            .takeIf { it.isNotBlank() }
                            ?: nextColor(usedColors)
                        usedColors.add(color)
                        Note(
                            id = id,
                            text = text,
                            createdAt = createdAt,
                            backgroundColorHex = color
                        )
                    }

                    is String -> {
                        val color = nextColor(usedColors)
                        usedColors.add(color)
                        Note(
                            id = UUID.randomUUID().toString(),
                            text = value,
                            createdAt = migrationBaseTime + index * 1_000L,
                            backgroundColorHex = color
                        )
                    }

                    else -> {
                        val color = nextColor(usedColors)
                        usedColors.add(color)
                        Note(
                            id = UUID.randomUUID().toString(),
                            text = value.toString(),
                            createdAt = migrationBaseTime + index * 1_000L,
                            backgroundColorHex = color
                        )
                    }
                }
            }
        }.getOrDefault(emptyList())
    }
}
