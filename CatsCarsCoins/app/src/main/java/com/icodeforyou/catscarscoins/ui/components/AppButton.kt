// ui/components/AppButton.kt
// CatsCarsCoins — spec 24.2.35. Complete file.
package com.icodeforyou.catscarscoins.ui.components

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.sp

/**
 * The app's one button (spec §17). The two behavior params are REQUIRED
 * with no defaults — callers read them at the call site from the global
 * accessors:
 * ```
 * AppButton(
 *     text = "Refresh",
 *     onClick = { ... },
 *     useHaptics = hapticsEnabled(),
 *     fontSize = buttonFontSize(),
 * )
 * ```
 * The button has no knowledge of preferences or the design system's
 * sources — it receives plain values and uses them. Haptics fire inside
 * onClick only: a button never buzzes on draw, and recomposition does not
 * invoke onClick.
 *
 * [modifier] defaults per standard Compose convention; the spec's
 * no-defaults rule covers the two behavior params.
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    useHaptics: Boolean,
    fontSize: Float,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current

    Button(
        onClick = {
            if (useHaptics) {
                haptics.performHapticFeedback(HapticFeedbackType.Confirm)
            }
            onClick()
        },
        modifier = modifier,
    ) {
        Text(
            text = text,
            fontSize = fontSize.sp,
        )
    }
}