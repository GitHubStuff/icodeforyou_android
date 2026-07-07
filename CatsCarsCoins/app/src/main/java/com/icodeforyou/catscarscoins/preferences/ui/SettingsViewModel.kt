// preferences/ui/SettingsViewModel.kt
// CatsCarsCoins — spec 24.5.14. Complete file.
// Change from 24.2.33: ResetAppUseCase injected + onNukeDatabase() — the
// Settings Nuke Database card's action (one tap, no confirmation, by
// decree). Cross-feature domain->domain import; the use case may move to
// a neutral package during the queued §16 rewrite.
package com.icodeforyou.catscarscoins.preferences.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icodeforyou.catscarscoins.dbviewer.domain.ResetAppUseCase
import com.icodeforyou.catscarscoins.preferences.domain.AppPreferences
import com.icodeforyou.catscarscoins.preferences.domain.PreferencesRepository
import com.icodeforyou.catscarscoins.preferences.domain.ThemeMode
import com.icodeforyou.catscarscoins.ui.SUBSCRIPTION_STOP_TIMEOUT_MS
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Write path for the four persisted preferences (spec: theme, polling
 * 5–30 s, pause, haptics) plus the destructive reset trigger. Exposes the
 * full [AppPreferences] as state — the Settings screen draws its controls
 * from one object — and intent-named actions that delegate outward. No
 * logic lives here: clamping belongs to the data layer per the 24.1.2
 * contract, and the wipe belongs to [ResetAppUseCase].
 */
class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val resetAppUseCase: ResetAppUseCase,
) : ViewModel() {

    val preferences: StateFlow<AppPreferences> = preferencesRepository.preferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_STOP_TIMEOUT_MS),
            initialValue = AppPreferences.DEFAULTS,
        )

    fun onThemeModeSelected(themeMode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(themeMode)
        }
    }

    fun onPollingIntervalChanged(seconds: Int) {
        viewModelScope.launch {
            preferencesRepository.setPollingIntervalSeconds(seconds)
        }
    }

    fun onPollingPausedChanged(paused: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setPollingPaused(paused)
        }
    }

    fun onHapticsEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setHapticsEnabled(enabled)
        }
    }

    /** Nuke Database: fires the wipe. No confirmation — by decree. */
    fun onNukeDatabase() {
        viewModelScope.launch {
            resetAppUseCase.execute()
        }
    }
}