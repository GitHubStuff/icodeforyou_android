// ui/StateFlowDefaults.kt
// CatsCarsCoins — spec 24.2.30. Complete file.
package com.icodeforyou.catscarscoins.ui

/**
 * App-wide stop-timeout for ViewModel StateFlows using
 * SharingStarted.WhileSubscribed: upstream collection survives a config
 * change (rotation re-subscribes within the window) but stops when
 * nothing observes for this long.
 *
 * Promoted from per-ViewModel duplicates at the third user (rule of two,
 * same discipline as the shared test infra): ThemeViewModel,
 * SettingsViewModel, CoinsViewModel all read this single value — the
 * app's subscription policy cannot drift apart per screen.
 */
const val SUBSCRIPTION_STOP_TIMEOUT_MS: Long = 5_000L