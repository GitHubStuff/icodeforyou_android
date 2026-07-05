// preferences/di/PreferencesModule.kt
// preferences/di/PreferencesModule.kt
// CatsCarsCoins — spec 24.1.9. Complete file.
// Change from 24.1.5: ThemeViewModel registered via viewModelOf.
package com.icodeforyou.catscarscoins.preferences.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.icodeforyou.catscarscoins.preferences.data.DataStorePreferencesRepository
import com.icodeforyou.catscarscoins.preferences.domain.PreferencesRepository
import com.icodeforyou.catscarscoins.preferences.ui.ThemeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** On-disk store name — appears exactly once. */
private const val PREFERENCES_STORE_NAME = "app_preferences"

/**
 * Process-wide DataStore instance. The [preferencesDataStore] property
 * delegate is the canonical way to obtain a Preferences DataStore: it
 * guarantees exactly one instance per file per process. Constructing the
 * store by hand inside a DI factory is how "multiple DataStores active for
 * the same file" crashes happen — the delegate makes that impossible.
 */
private val Context.appPreferencesDataStore: DataStore<Preferences>
        by preferencesDataStore(name = PREFERENCES_STORE_NAME)

/**
 * Preferences feature wiring (spec: KOIN runtime DSL, no codegen).
 * Consumers inject [PreferencesRepository] — the domain contract — and
 * never see DataStore types (Dependency Inversion).
 */
val preferencesModule = module {

    single<DataStore<Preferences>> {
        androidContext().appPreferencesDataStore
    }

    single<PreferencesRepository> {
        DataStorePreferencesRepository(dataStore = get())
    }

    viewModelOf(::ThemeViewModel)
}