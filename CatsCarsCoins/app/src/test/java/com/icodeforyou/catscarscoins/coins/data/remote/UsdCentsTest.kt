// coins/data/remote/UsdCentsTest.kt
// CatsCarsCoins — spec 24.2.15. Complete file. Test sources.
package com.icodeforyou.catscarscoins.coins.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

class UsdCentsTest {

    @Test
    fun `two decimal places parse exactly`() {
        assertEquals(434317L, "4343.17".toUsdCents())
    }

    @Test
    fun `one decimal place fills to cents`() {
        assertEquals(434310L, "4343.1".toUsdCents())
    }

    @Test
    fun `no decimal point fills to cents`() {
        assertEquals(434300L, "4343".toUsdCents())
    }

    @Test
    fun `single cent parses exactly`() {
        assertEquals(1L, "0.01".toUsdCents())
    }

    @Test
    fun `zero parses to zero`() {
        assertEquals(0L, "0".toUsdCents())
    }

    @Test
    fun `double-hostile value parses exactly`() {
        assertEquals(2903L, "29.03".toUsdCents())
    }

    @Test
    fun `large market price parses exactly`() {
        assertEquals(6_712_345L, "67123.45".toUsdCents())
    }

    @Test
    fun `three decimal places round half up`() {
        assertEquals(434318L, "4343.175".toUsdCents())
    }

    @Test(expected = NumberFormatException::class)
    fun `alphabetic input throws`() {
        "abc".toUsdCents()
    }

    @Test(expected = NumberFormatException::class)
    fun `empty input throws`() {
        "".toUsdCents()
    }
}