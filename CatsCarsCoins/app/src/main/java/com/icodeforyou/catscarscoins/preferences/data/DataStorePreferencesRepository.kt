// preferences/data/DataStorePreferencesRepository.kt
// CatsCarsCoins — spec 24.1.4. Complete file.
package com.icodeforyou.catscarscoins.preferences.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.icodeforyou.catscarscoins.preferences.domain.AppPreferences
import com.icodeforyou.catscarscoins.preferences.domain.PreferencesRepository
import com.icodeforyou.catscarscoins.preferences.domain.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Preferences DataStore implementation of [PreferencesRepository]
 * (spec 24.1). The store is constructor-injected: production wiring (Koin)
 * and tests each supply their own [DataStore], so this class owns mapping
 * and nothing else (SRP).
 *
 * An empty store maps to [AppPreferences.DEFAULTS], which makes
 * [resetToDefaults] a plain [Preferences.clear] — reset and first-run are
 * the same code path by construction, not by discipline.
 */
class DataStorePreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) : PreferencesRepository {

    override val preferences: Flow<AppPreferences> =
        dataStore.data.map(::toDomain)

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { store -> store[KEY_THEME_MODE] = themeMode.name }
    }

    override suspend fun setPollingIntervalSeconds(seconds: Int) {
        val clamped = seconds.coerceIn(AppPreferences.POLLING_INTERVAL_RANGE)
        dataStore.edit { store -> store[KEY_POLLING_INTERVAL_SECONDS] = clamped }
    }

    override suspend fun setPollingPaused(paused: Boolean) {
        dataStore.edit { store -> store[KEY_POLLING_PAUSED] = paused }
    }

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        dataStore.edit { store -> store[KEY_HAPTICS_ENABLED] = enabled }
    }

    override suspend fun resetToDefaults() {
        dataStore.edit { store -> store.clear() }
    }

    private fun toDomain(stored: Preferences): AppPreferences =
        AppPreferences(
            themeMode = stored[KEY_THEME_MODE].toThemeMode(),
            pollingIntervalSeconds = stored[KEY_POLLING_INTERVAL_SECONDS]
                ?.coerceIn(AppPreferences.POLLING_INTERVAL_RANGE)
                ?: AppPreferences.DEFAULTS.pollingIntervalSeconds,
            pollingPaused = stored[KEY_POLLING_PAUSED]
                ?: AppPreferences.DEFAULTS.pollingPaused,
            hapticsEnabled = stored[KEY_HAPTICS_ENABLED]
                ?: AppPreferences.DEFAULTS.hapticsEnabled,
        )

    private fun String?.toThemeMode(): ThemeMode =
        this?.let { name ->
            ThemeMode.entries.firstOrNull { it.name == name }
        } ?: AppPreferences.DEFAULTS.themeMode

    private companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_POLLING_INTERVAL_SECONDS = intPreferencesKey("polling_interval_seconds")
        val KEY_POLLING_PAUSED = booleanPreferencesKey("polling_paused")
        val KEY_HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
    }
}