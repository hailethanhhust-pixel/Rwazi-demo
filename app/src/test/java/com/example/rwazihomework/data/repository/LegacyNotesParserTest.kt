package com.example.rwazihomework.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyNotesParserTest {

    @Test
    fun parse_returnsEmpty_whenRawIsNull() {
        val result = LegacyNotesParser.parse(raw = null, nextColor = { "#111111" })
        assertTrue(result.isEmpty())
    }

    @Test
    fun parse_preservesStructuredFields_whenJsonObjectsAreProvided() {
        val raw = """
            [
              {
                                "id":"note-a",
                                "text":"Hello",
                                "createdAt":1234,
                                "backgroundColorHex":"#ABCDEF"
              }
            ]
        """.trimIndent()

        val result = LegacyNotesParser.parse(raw = raw, nextColor = { "#111111" })

        assertEquals(1, result.size)
        assertEquals("note-a", result.first().id)
        assertEquals("Hello", result.first().text)
        assertEquals(1234L, result.first().createdAt)
        assertEquals("#ABCDEF", result.first().backgroundColorHex)
    }

    @Test
    fun parse_supportsLegacyStringEntries() {
        val raw = "[\"one\", \"two\"]"

        val result = LegacyNotesParser.parse(
            raw = raw,
            nowMillis = 2_000L,
            nextColor = { used -> "#C${used.size}" }
        )

        assertEquals(2, result.size)
        assertEquals("one", result[0].text)
        assertEquals("two", result[1].text)
        assertEquals("#C0", result[0].backgroundColorHex)
        assertEquals("#C1", result[1].backgroundColorHex)
    }
}
