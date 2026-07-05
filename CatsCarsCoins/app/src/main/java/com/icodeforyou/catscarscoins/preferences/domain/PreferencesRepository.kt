// preferences/domain/PreferencesRepository.kt
// CatsCarsCoins — spec 24.1.2. Complete file.
package com.icodeforyou.catscarscoins.preferences.domain

import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for the four persisted preferences (spec: theme, polling
 * interval 5–30 s, polling pause, haptics). Pure Kotlin — implementations
 * live in the data layer (Preferences DataStore); consumers depend only on
 * this interface (Dependency Inversion).
 *
 * One setter per key: callers state intent for exactly the value they are
 * changing; there is no whole-object save to race against concurrent writes.
 */
interface PreferencesRepository {

    /**
     * The current preferences, emitting on every change. First emission is
     * [AppPreferences.DEFAULTS] when no value has ever been written.
     */
    val preferences: Flow<AppPreferences>

    suspend fun setThemeMode(themeMode: ThemeMode)

    /**
     * Persists the Coins polling interval. Implementations MUST clamp
     * [seconds] into [AppPreferences.POLLING_INTERVAL_RANGE]; out-of-range
     * input is a caller bug that must not corrupt stored state.
     */
    suspend fun setPollingIntervalSeconds(seconds: Int)

    suspend fun setPollingPaused(paused: Boolean)

    suspend fun setHapticsEnabled(enabled: Boolean)

    /**
     * Restores [AppPreferences.DEFAULTS] (spec §16 Reset App, step 2:
     * theme=dark, haptics=on, polling=5, pause=true). Invoked by
     * ResetAppUseCase; also the definition of first-run state.
     */
    suspend fun resetToDefaults()
}