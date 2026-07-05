// nav/AppNavKeys.kt
// CatsCarsCoins — spec 24.1.24. Complete file.
// Change from previous version: SettingsKey added (Phase 1 destination).
package com.icodeforyou.catscarscoins.nav

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * App-level Nav3 keys (spec 0.5: NavDisplay Splash -> Main -> empty stubs).
 * Keys are @Serializable so the developer-owned back stack survives process
 * death (spec §5: kotlinx.serialization also serializes NavKeys). Feature
 * keys live in each feature's own nav/ package — this sealed interface only
 * covers app-shell destinations.
 */
sealed interface AppNavKey : NavKey

@Serializable
data object SplashKey : AppNavKey

@Serializable
data object MainKey : AppNavKey

@Serializable
data object SettingsKey : AppNavKey