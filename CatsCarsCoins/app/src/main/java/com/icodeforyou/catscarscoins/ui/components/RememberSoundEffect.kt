// ui/components/RememberSoundEffect.kt
// CatsCarsCoins — Complete file.
// One-shot sound effect player over SoundPool. Loads the raw resource at
// composition (SoundPool loading is async — loading at tap time would
// play nothing), exposes a fire-and-forget play lambda, and releases the
// pool when the composable leaves composition. Taps before the load
// completes are silently dropped rather than deferred.
package com.icodeforyou.catscarscoins.ui.components

import android.media.AudioAttributes
import android.media.SoundPool
import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

private const val MAX_STREAMS = 1
private const val LOAD_PRIORITY = 1
private const val PLAY_PRIORITY = 1
private const val NO_LOOP = 0
private const val FULL_VOLUME = 1f
private const val NORMAL_RATE = 1f
private const val LOAD_SUCCESS = 0

/**
 * Returns a lambda that plays [resId] once at full volume. Safe to call
 * repeatedly; each call restarts the sound (single stream).
 */
@Composable
fun rememberSoundEffect(@RawRes resId: Int): () -> Unit {
    val context = LocalContext.current

    val soundPool = remember {
        SoundPool.Builder()
            .setMaxStreams(MAX_STREAMS)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            .build()
    }

    var soundId by remember { mutableIntStateOf(0) }
    var loaded by remember { mutableStateOf(false) }

    DisposableEffect(resId) {
        soundPool.setOnLoadCompleteListener { _, id, status ->
            if (status == LOAD_SUCCESS && id == soundId) {
                loaded = true
            }
        }
        soundId = soundPool.load(context, resId, LOAD_PRIORITY)

        onDispose {
            soundPool.release()
        }
    }

    return {
        if (loaded) {
            soundPool.play(
                soundId,
                FULL_VOLUME,
                FULL_VOLUME,
                PLAY_PRIORITY,
                NO_LOOP,
                NORMAL_RATE,
            )
        }
    }
}