// app/src/test/java/com/icodeforyou/catscarscoins/cars/data/CarDtoTest.kt
// Test sources.
package com.icodeforyou.catscarscoins.cars.data

import com.icodeforyou.catscarscoins.cars.domain.Manufacturer
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CarDtoTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `decodes vPIC envelope and ignores unknown keys`() {
        val payload = """
            {
              "Count": 2,
              "Message": "Response returned successfully",
              "SearchCriteria": null,
              "Results": [
                {
                  "Country": "UNITED STATES (USA)",
                  "Mfr_CommonName": "Tesla",
                  "Mfr_ID": 955,
                  "Mfr_Name": "TESLA, INC.",
                  "VehicleTypes": [ { "IsPrimary": true, "Name": "Passenger Car" } ]
                },
                {
                  "Country": "GERMANY",
                  "Mfr_CommonName": "BMW",
                  "Mfr_ID": 967,
                  "Mfr_Name": "BMW AG",
                  "VehicleTypes": []
                }
              ]
            }
        """.trimIndent()

        val response = json.decodeFromString<ManufacturersResponseDto>(payload)

        assertEquals(2, response.results.size)
        assertEquals(
            CarDto(
                mfrId = 955,
                mfrName = "TESLA, INC.",
                country = "UNITED STATES (USA)",
            ),
            response.results.first(),
        )
    }

    @Test
    fun `toDomainOrNull maps id name country`() {
        val dto = CarDto(
            mfrId = 955,
            mfrName = "TESLA, INC.",
            country = "UNITED STATES (USA)",
        )

        assertEquals(
            Manufacturer(
                id = 955,
                name = "TESLA, INC.",
                country = "UNITED STATES (USA)",
            ),
            dto.toDomainOrNull(),
        )
    }

    @Test
    fun `null Country decodes and maps to blank country default`() {
        val payload = """
            {
              "Country": null,
              "Mfr_ID": 1234,
              "Mfr_Name": "SOMEWHERE MOTORS"
            }
        """.trimIndent()

        val dto = json.decodeFromString<CarDto>(payload)

        assertEquals(
            Manufacturer(
                id = 1234,
                name = "SOMEWHERE MOTORS",
                country = "",
            ),
            dto.toDomainOrNull(),
        )
    }

    @Test
    fun `missing Country key decodes and maps to blank country default`() {
        val payload = """
            {
              "Mfr_ID": 1234,
              "Mfr_Name": "SOMEWHERE MOTORS"
            }
        """.trimIndent()

        val dto = json.decodeFromString<CarDto>(payload)

        assertEquals(
            Manufacturer(
                id = 1234,
                name = "SOMEWHERE MOTORS",
                country = "",
            ),
            dto.toDomainOrNull(),
        )
    }

    @Test
    fun `blank Country maps to blank country default`() {
        val dto = CarDto(
            mfrId = 1234,
            mfrName = "SOMEWHERE MOTORS",
            country = "   ",
        )

        assertEquals("", dto.toDomainOrNull()?.country)
    }

    @Test
    fun `null Mfr_ID returns null`() {
        val payload = """
            {
              "Country": "GERMANY",
              "Mfr_Name": "BMW AG"
            }
        """.trimIndent()

        val dto = json.decodeFromString<CarDto>(payload)

        assertNull(dto.toDomainOrNull())
    }

    @Test
    fun `null Mfr_Name returns null`() {
        val payload = """
            {
              "Country": "GERMANY",
              "Mfr_ID": 967
            }
        """.trimIndent()

        val dto = json.decodeFromString<CarDto>(payload)

        assertNull(dto.toDomainOrNull())
    }

    @Test
    fun `blank Mfr_Name returns null`() {
        val dto = CarDto(
            mfrId = 967,
            mfrName = "   ",
            country = "GERMANY",
        )

        assertNull(dto.toDomainOrNull())
    }
}