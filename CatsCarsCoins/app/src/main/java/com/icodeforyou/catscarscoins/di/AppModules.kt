// di/AppModules.kt
// CatsCarsCoins — spec 24.1.6. Complete file.
// Change from previous version: preferencesModule is the first feature
// module wired into the aggregate (Phase 1).
package com.icodeforyou.catscarscoins.di

import com.icodeforyou.catscarscoins.preferences.di.preferencesModule
import org.koin.core.module.Module

/**
 * Aggregates every feature's Koin module (spec §3: one module per feature,
 * AppModules aggregates). Each feature phase appends its module here — the
 * only DI file MyApp ever needs to know about.
 */
val appModules: List<Module> = listOf(
    preferencesModule,
)