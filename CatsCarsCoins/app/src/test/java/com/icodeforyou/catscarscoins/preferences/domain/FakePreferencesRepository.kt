// preferences/domain/FakePreferencesRepository.kt
// CatsCarsCoins — spec 24.1.17. Complete file. Test sources.
package com.icodeforyou.catscarscoins.preferences.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Canonical in-memory fake of [PreferencesRepository] for unit tests.
 * Lives in the test source set, same package as the contract it fakes.
 * Behaves like the real implementation over a [MutableStateFlow]: setters
 * emit, [setPollingIntervalSeconds] clamps per the 24.1.2 contract, and
 * [resetToDefaults] restores [AppPreferences.DEFAULTS].
 */
class FakePreferencesRepository(
    initial: AppPreferences = AppPreferences.DEFAULTS,
) : PreferencesRepository {

    private val state = MutableStateFlow(initial)

    override val preferences: Flow<AppPreferences> = state

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        state.update { it.copy(themeMode = themeMode) }
    }

    override suspend fun setPollingIntervalSeconds(seconds: Int) {
        val clamped = seconds.coerceIn(AppPreferences.POLLING_INTERVAL_RANGE)
        state.update { it.copy(pollingIntervalSeconds = clamped) }
    }

    override suspend fun setPollingPaused(paused: Boolean) {
        state.update { it.copy(pollingPaused = paused) }
    }

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        state.update { it.copy(hapticsEnabled = enabled) }
    }

    override suspend fun resetToDefaults() {
        state.value = AppPreferences.DEFAULTS
    }
}