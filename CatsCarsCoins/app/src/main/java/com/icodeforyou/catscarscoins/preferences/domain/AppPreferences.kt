// preferences/domain/AppPreferences.kt
// CatsCarsCoins — spec 24.1.1. Complete file.
package com.icodeforyou.catscarscoins.preferences.domain

/**
 * Theme selection (spec §18): two explicit palettes plus a system-following
 * mode. SYSTEM resolves at composition time via isSystemInDarkTheme() —
 * resolution is a UI concern and deliberately absent from this layer.
 */
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM,
}

/**
 * The four persisted preferences (spec §Decision Log: "theme, polling
 * (5–30 int), pause, haptics"). Pure domain model — no Android, no
 * DataStore, no serialization knowledge. The data layer maps this to and
 * from Preferences DataStore keys; everything above consumes only this type.
 */
data class AppPreferences(
    val themeMode: ThemeMode,
    val pollingIntervalSeconds: Int,
    val pollingPaused: Boolean,
    val hapticsEnabled: Boolean,
) {
    companion object {

        /** Inclusive polling bounds (spec: 5 – 30 seconds, integer). */
        const val POLLING_INTERVAL_MIN_SECONDS: Int = 5
        const val POLLING_INTERVAL_MAX_SECONDS: Int = 30

        const val POLLING_INTERVAL_DEFAULT: Int = 10

        val POLLING_INTERVAL_RANGE: IntRange =
            POLLING_INTERVAL_MIN_SECONDS..POLLING_INTERVAL_MAX_SECONDS

        /**
         * Spec reset/default state (spec §16 Reset App order step 2):
         * theme=dark, haptics=on, polling=10, pause=true. First run uses
         * polling_interval_default = 10 secconds
         */
        val DEFAULTS: AppPreferences = AppPreferences(
            themeMode = ThemeMode.DARK,
            pollingIntervalSeconds = POLLING_INTERVAL_DEFAULT,
            pollingPaused = true,
            hapticsEnabled = true,
        )
    }
}