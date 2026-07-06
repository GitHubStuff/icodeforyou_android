// cats/data/remote/CatDtoTest.kt
// CatsCarsCoins — spec 24.3.11. Complete file. Test sources.
package com.icodeforyou.catscarscoins.cats.data.remote

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CatDtoTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `full payload decodes and maps every field`() {
        val payload = """
            {
              "id": "abc",
              "url": "https://cdn.example/abc.jpg",
              "width": 1200,
              "height": 800,
              "breeds": [
                {
                  "id": "beng",
                  "name": "Bengal",
                  "origin": "United States",
                  "temperament": "Alert, Agile",
                  "life_span": "12 - 15",
                  "description": "Bengals are a lot of fun",
                  "wikipedia_url": "https://en.wikipedia.org/wiki/Bengal_cat"
                }
              ]
            }
        """.trimIndent()

        val cat = json.decodeFromString<CatImageDto>(payload).toDomainOrNull()

        assertEquals("abc", cat?.id)
        assertEquals("https://cdn.example/abc.jpg", cat?.imageUrl)
        assertEquals("beng", cat?.breed?.id)
        assertEquals("Bengal", cat?.breed?.name)
        assertEquals("United States", cat?.breed?.origin)
        assertEquals("Alert, Agile", cat?.breed?.temperament)
        assertEquals("12 - 15", cat?.breed?.lifeSpan)
        assertEquals("Bengals are a lot of fun", cat?.breed?.description)
    }

    @Test
    fun `missing optional breed fields decode to empty strings`() {
        val payload = """
            {
              "id": "abc",
              "url": "https://cdn.example/abc.jpg",
              "breeds": [ { "id": "beng", "name": "Bengal" } ]
            }
        """.trimIndent()

        val cat = json.decodeFromString<CatImageDto>(payload).toDomainOrNull()

        assertEquals("", cat?.breed?.origin)
        assertEquals("", cat?.breed?.temperament)
        assertEquals("", cat?.breed?.lifeSpan)
        assertEquals("", cat?.breed?.description)
    }

    @Test
    fun `entry without breeds maps to null`() {
        val payload = """
            { "id": "abc", "url": "https://cdn.example/abc.jpg", "breeds": [] }
        """.trimIndent()

        assertNull(json.decodeFromString<CatImageDto>(payload).toDomainOrNull())
    }

    @Test
    fun `entry with absent breeds key maps to null`() {
        val payload = """
            { "id": "abc", "url": "https://cdn.example/abc.jpg" }
        """.trimIndent()

        assertNull(json.decodeFromString<CatImageDto>(payload).toDomainOrNull())
    }

    @Test
    fun `first breed wins when several are present`() {
        val payload = """
            {
              "id": "abc",
              "url": "https://cdn.example/abc.jpg",
              "breeds": [
                { "id": "beng", "name": "Bengal" },
                { "id": "siam", "name": "Siamese" }
              ]
            }
        """.trimIndent()

        val cat = json.decodeFromString<CatImageDto>(payload).toDomainOrNull()

        assertEquals("Bengal", cat?.breed?.name)
    }
}