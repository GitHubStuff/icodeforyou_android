// notifier/NotifierHost.kt
package com.icodeforyou.catscarscoins.notifier

import androidx.compose.runtime.Composable

/**
 * Placeholder host for the custom in-app Notifier (spec §5: arbitrary
 * composable payload, dismissible; first built for real in Phase 2).
 * At standup it renders content untouched — the call site in MainActivity
 * is wired now so Phase 2 replaces only this body, never the callers (OCP).
 */
@Composable
fun NotifierHost(content: @Composable () -> Unit) {
    content()
}