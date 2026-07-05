// preferences/PreferenceAccessorsTest.kt
// CatsCarsCoins — spec 24.1.14. Complete file.
package com.icodeforyou.catscarscoins.preferences

import com.icodeforyou.catscarscoins.preferences.domain.AppPreferences
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PreferenceAccessorsTest {

    @After
    fun tearDown() {
        PreferencesSnapshot.update(AppPreferences.DEFAULTS)
    }

    @Test
    fun `hapticsEnabled reflects the spec default before any update`() {
        assertTrue(hapticsEnabled())
    }

    @Test
    fun `hapticsEnabled reflects a snapshot update`() {
        PreferencesSnapshot.update(
            AppPreferences.DEFAULTS.copy(hapticsEnabled = false),
        )

        assertFalse(hapticsEnabled())
    }

    @Test
    fun `hapticsEnabled follows repeated updates`() {
        PreferencesSnapshot.update(
            AppPreferences.DEFAULTS.copy(hapticsEnabled = false),
        )
        PreferencesSnapshot.update(
            AppPreferences.DEFAULTS.copy(hapticsEnabled = true),
        )

        assertTrue(hapticsEnabled())
    }

    @Test
    fun `buttonFontSize returns the design token`() {
        assertEquals(BUTTON_FONT_SIZE_SP, buttonFontSize(), 0.0f)
    }
}