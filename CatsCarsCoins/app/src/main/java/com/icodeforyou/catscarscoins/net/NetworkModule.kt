// net/NetworkModule.kt
// CatsCarsCoins — spec 24.2.18. Complete file.
package com.icodeforyou.catscarscoins.net

import com.icodeforyou.catscarscoins.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module

/**
 * App-wide network primitives (spec §5: Retrofit + OkHttp +
 * kotlinx.serialization). One OkHttpClient and one Json configuration for
 * the whole app; each feature builds its own Retrofit on top with its own
 * base URL (Coins next step, Cars/VPIC in Phase 4) — connection pool and
 * JSON policy stay single-sourced.
 */
val networkModule = module {

    single<Json> {
        Json {
            // Tolerate additive API changes: unknown wire fields are
            // ignored instead of crashing deserialization.
            ignoreUnknownKeys = true
        }
    }

    single<OkHttpClient> {
        OkHttpClient.Builder()
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().setLevel(
                            HttpLoggingInterceptor.Level.BASIC,
                        ),
                    )
                }
            }
            .build()
    }
}
