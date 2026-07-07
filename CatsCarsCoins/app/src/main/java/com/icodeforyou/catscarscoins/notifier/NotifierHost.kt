// notifier/NotifierHost.kt
// CatsCarsCoins — spec 24.2.41. Complete file.
// Change from the 0.5 placeholder: the host has its real body — overlay
// surface, whole-surface + [X] dismissal, and the LaunchedEffect clock
// keyed on Notification.id.
// Correction folded in (24.2.52 splash integrity): enabled param — while
// false (splash), the overlay neither renders nor runs its clock; a
// notification arriving during splash shows after it, full duration.
// Also folded: delay(Long) is the legacy overload — Duration form used.
package com.icodeforyou.catscarscoins.notifier

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

private val TOAST_MARGIN = 16.dp
private val TOAST_CONTENT_PADDING = 12.dp
private val TOAST_ELEVATION = 6.dp

/**
 * The in-app notification overlay (spec: arbitrary composable payloads,...)
 * auto-dismiss after the notification's duration, tap-dismissible via the
 * whole surface or the X. Sits exactly where spec 0.5 parked it —
 * AppTheme { NavigationSuiteScaffold { NotifierHost { NavDisplay } } } —
 * so toasts overlay the content area on every destination, above nothing
 * and hiding nothing but what they cover.
 *
 * The dismiss clock is a LaunchedEffect keyed on [Notifier.Notification.id]:
 * every show carries a fresh id, so replace-on-collision restarts the
 * timer even for two identical payloads (pinned by 24.2.39).
 */
@Composable
fun NotifierHost(
    enabled: Boolean = true,
    notifier: Notifier = koinInject(),
    content: @Composable () -> Unit,
) {
    val notification by notifier.current.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        content()

        if (enabled) {
            notification?.let { current ->
                LaunchedEffect(current.id) {
                    delay(current.durationMillis.milliseconds)
                    notifier.dismiss()
                }
                NotificationSurface(
                    notification = current,
                    onDismiss = notifier::dismiss,
                )
            }
        }
    }
}

@Composable
private fun BoxScope.NotificationSurface(
    notification: Notifier.Notification,
    onDismiss: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(TOAST_MARGIN)
            .clickable(onClick = onDismiss),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = TOAST_ELEVATION,
        shadowElevation = TOAST_ELEVATION,
        border = notification.border,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.padding(TOAST_CONTENT_PADDING)) {
                notification.content()
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss")
            }
        }
    }
}