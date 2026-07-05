// preferences/ui/ThemeViewModel.kt
// CatsCarsCoins — spec 24.1.8. Complete file.
// Correction folded in: Flow.map takes a suspend transform; Kotlin does not
// adapt property references (AppPreferences::themeMode) to suspend function
// types, so the transform is a plain lambda.
package com.icodeforyou.catscarscoins.preferences.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icodeforyou.catscarscoins.preferences.domain.AppPreferences
import com.icodeforyou.catscarscoins.preferences.domain.PreferencesRepository
import com.icodeforyou.catscarscoins.preferences.domain.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Upstream collection survives a config change (rotation re-subscribes
 * within the window) but stops when nothing observes for this long.
 */
private const val SUBSCRIPTION_STOP_TIMEOUT_MS = 5_000L

/**
 * Spec §18 pipeline, middle piece: DataStore → StateFlow → MaterialTheme.
 * Sole responsibility: expose the persisted [ThemeMode] as state (SRP).
 * StateFlow's equality conflation guarantees non-theme preference writes
 * never emit — the theme tree does not recompose when haptics or polling
 * change (verified by 24.1.7).
 */
class ThemeViewModel(
    preferencesRepository: PreferencesRepository,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = preferencesRepository.preferences
        .map { preferences -> preferences.themeMode }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_STOP_TIMEOUT_MS),
            initialValue = AppPreferences.DEFAULTS.themeMode,
        )
}