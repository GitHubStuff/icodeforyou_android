// di/AppModules.kt
package com.icodeforyou.catscarscoins.di

import org.koin.core.module.Module

/**
 * Aggregates every feature's Koin module (spec §3: one module per feature,
 * AppModules aggregates). Empty at standup (spec 0.5); each feature phase
 * appends its module here — the only DI file MyApp ever needs to know about.
 */
val appModules: List<Module> = emptyList()