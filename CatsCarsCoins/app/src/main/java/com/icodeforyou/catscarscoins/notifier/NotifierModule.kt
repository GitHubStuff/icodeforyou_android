// notifier/NotifierModule.kt
// CatsCarsCoins — spec 24.2.42. Complete file.
package com.icodeforyou.catscarscoins.notifier

import org.koin.dsl.module

/**
 * Notifier wiring: one process-wide [Notifier] — a single slot shared by
 * every producer (coin toast, read-only toast, whatever comes later) and
 * the one NotifierHost that renders it. Two instances would mean two
 * competing collision policies; the single IS the spec's
 * replace-on-collision at the object level.
 */
val notifierModule = module {

    single {
        Notifier()
    }
}