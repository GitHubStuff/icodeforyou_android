// cats/di/CatsModule.kt
// CatsCarsCoins — spec 24.3.20. Complete file.
// Change from 24.3.16: CatsViewModel registered via viewModelOf.
package com.icodeforyou.catscarscoins.cats.di

import com.icodeforyou.catscarscoins.BuildConfig
import com.icodeforyou.catscarscoins.cats.data.RoomCatsRepository
import com.icodeforyou.catscarscoins.cats.data.remote.CatApi
import com.icodeforyou.catscarscoins.cats.data.remote.CatApiRemoteSource
import com.icodeforyou.catscarscoins.cats.domain.CatsRemoteSource
import com.icodeforyou.catscarscoins.cats.domain.CatsRepository
import com.icodeforyou.catscarscoins.cats.ui.CatsViewModel
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** The Cat API auth header — appears exactly once, right here. */
private const val API_KEY_HEADER = "x-api-key"

/** Converter media type — fixed by HTTP/JSON standards per call site. */
private const val JSON_MEDIA_TYPE = "application/json"

/**
 * Cats feature wiring (spec: Koin runtime DSL). Consumers inject domain
 * contracts; Room, Retrofit, and wire types stay behind this file (DIP).
 * CatDao resolves from databaseModule; OkHttpClient and Json from
 * networkModule.
 */
val catsModule = module {

    single<CatsRepository> {
        RoomCatsRepository(
            catDao = get(),
            remoteSource = get(),
        )
    }

    single<CatApi> {
        // newBuilder(): shares the base client's pool and dispatcher;
        // the credential interceptor exists only on this host's client.
        // BuildConfig.CAT_API_KEY's single appearance in the codebase.
        val authenticatedClient = get<OkHttpClient>().newBuilder()
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header(API_KEY_HEADER, BuildConfig.CAT_API_KEY)
                        .build(),
                )
            }
            .build()

        Retrofit.Builder()
            .baseUrl(CatApi.BASE_URL)
            .client(authenticatedClient)
            .addConverterFactory(
                get<Json>().asConverterFactory(JSON_MEDIA_TYPE.toMediaType()),
            )
            .build()
            .create(CatApi::class.java)
    }

    single<CatsRemoteSource> {
        CatApiRemoteSource(catApi = get())
    }

    viewModelOf(::CatsViewModel)
}