// notifier/NotifierTest.kt
// CatsCarsCoins — spec 24.2.39. Complete file. Test sources.
package com.icodeforyou.catscarscoins.notifier

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NotifierTest {

    @Test
    fun `initially shows nothing`() {
        val notifier = Notifier()

        assertNull(notifier.current.value)
    }

    @Test
    fun `show sets the current notification with its duration`() {
        val notifier = Notifier()

        notifier.show(durationMillis = 1_250L) { }

        assertEquals(1_250L, notifier.current.value?.durationMillis)
    }

    @Test
    fun `show replaces the current notification on collision`() {
        val notifier = Notifier()
        notifier.show(durationMillis = 1_250L) { }
        val first = notifier.current.value

        notifier.show(durationMillis = 750L) { }
        val second = notifier.current.value

        assertEquals(750L, second?.durationMillis)
        assertNotEquals(first?.id, second?.id)
    }

    @Test
    fun `identical consecutive shows still get distinct ids`() {
        val notifier = Notifier()

        notifier.show(durationMillis = 1_250L) { }
        val first = notifier.current.value
        notifier.show(durationMillis = 1_250L) { }
        val second = notifier.current.value

        assertNotEquals(first?.id, second?.id)
    }

    @Test
    fun `dismiss clears the current notification`() {
        val notifier = Notifier()
        notifier.show(durationMillis = 1_250L) { }

        notifier.dismiss()

        assertNull(notifier.current.value)
    }

    @Test
    fun `dismiss after a replace clears the latest`() {
        val notifier = Notifier()
        notifier.show(durationMillis = 1_250L) { }
        notifier.show(durationMillis = 750L) { }

        notifier.dismiss()

        assertNull(notifier.current.value)
    }

    @Test
    fun `spec toast durations are the named constants`() {
        assertEquals(1_250L, COIN_TOAST_MILLIS)
        assertEquals(750L, READ_ONLY_TOAST_MILLIS)
    }
}