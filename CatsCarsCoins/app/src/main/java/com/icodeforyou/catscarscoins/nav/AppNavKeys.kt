// nav/AppNavKeys.kt
// CatsCarsCoins — spec 24.3.22. Complete file.
// Change from 24.2.37: CatsKey added (Phase 3 destination). Key order
// mirrors the rail: Main, Cats, Coins, Settings (Cars joins in Phase 4).
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
data object CatsKey : AppNavKey

@Serializable
data object CoinsKey : AppNavKey

@Serializable
data object SettingsKey : AppNavKey