// notifier/Notifier.kt
// CatsCarsCoins — spec 24.2.40. Complete file.
// Change: optional per-notification [Notification.border] — the host
// draws it on the toast surface. Default null keeps existing callers
// borderless and untouched.
package com.icodeforyou.catscarscoins.notifier

import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Composable
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Spec toast durations. */
const val COIN_TOAST_MILLIS: Long = 1_250L
const val READ_ONLY_TOAST_MILLIS: Long = 750L

/**
 * The in-app notifier core (spec: arbitrary composable payloads,
 * replace-on-collision, tap-dismissible). Pure state — no clocks, no
 * coroutines: auto-dismiss timing belongs to the NotifierHost's
 * LaunchedEffect keyed on [Notification.id], which is why every [show]
 * stamps a fresh monotonic id — two identical consecutive toasts must
 * still restart the clock.
 */
class Notifier {

    /**
     * One live notification. [id] exists solely to make effect keys and
     * equality distinguish repeat shows; [content] is any composable.
     */
    class Notification(
        val id: Long,
        val durationMillis: Long,
        val border: BorderStroke? = null,
        val content: @Composable () -> Unit,
    )

    private val nextId = AtomicLong(0L)

    private val _current = MutableStateFlow<Notification?>(null)
    val current: StateFlow<Notification?> = _current.asStateFlow()

    /** Replace-on-collision: whatever is showing is superseded, always. */
    fun show(
        durationMillis: Long,
        border: BorderStroke? = null,
        content: @Composable () -> Unit,
    ) {
        _current.value = Notification(
            id = nextId.incrementAndGet(),
            durationMillis = durationMillis,
            border = border,
            content = content,
        )
    }

    fun dismiss() {
        _current.value = null
    }
}