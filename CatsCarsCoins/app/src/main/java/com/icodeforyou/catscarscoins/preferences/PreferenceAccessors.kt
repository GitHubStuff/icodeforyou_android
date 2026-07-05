// preferences/PreferenceAccessors.kt
// CatsCarsCoins — spec 24.1.15. Complete file.
package com.icodeforyou.catscarscoins.preferences

import com.icodeforyou.catscarscoins.preferences.domain.AppPreferences

/**
 * Global button text size in sp (spec §17: "current global button text
 * size"). Not one of the four persisted keys — a design token. Matches
 * Material3 labelLarge (14 sp), the default button label size.
 */
const val BUTTON_FONT_SIZE_SP: Float = 14f

/**
 * Process-wide snapshot of the latest [AppPreferences] (spec §17: the
 * accessors below are plain top-level synchronous functions, so a
 * synchronous source must exist). MyApp feeds it: an application-scoped
 * collector pushes every repository emission through [update].
 *
 * This is deliberate, spec-mandated global state — the price of
 * synchronous call-site reads. Its discipline: exactly one writer (the
 * MyApp collector; tests reset via [update] in teardown), reads are
 * volatile, and nothing outside this file exposes the mutable field.
 */
object PreferencesSnapshot {

    @Volatile
    private var current: AppPreferences = AppPreferences.DEFAULTS

    fun update(preferences: AppPreferences) {
        current = preferences
    }

    internal fun read(): AppPreferences = current
}

/**
 * Spec §17 accessor: buzz-at-tap value, read by button callers at the call
 * site — `AppButton(useHaptics = hapticsEnabled(), ...)`. Read in onClick;
 * buttons never buzz on draw.
 */
fun hapticsEnabled(): Boolean = PreferencesSnapshot.read().hapticsEnabled

/**
 * Spec §17 accessor: current global button text size, read by button
 * callers at the call site — `AppButton(fontSize = buttonFontSize(), ...)`.
 * Sourced from the design token today; the uniform accessor is the OCP
 * seam that lets the source change without touching any button.
 */
fun buttonFontSize(): Float = BUTTON_FONT_SIZE_SP