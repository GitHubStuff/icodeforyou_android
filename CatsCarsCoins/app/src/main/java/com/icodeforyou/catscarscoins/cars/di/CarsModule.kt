// cars/di/CarsModule.kt
// CatsCarsCoins — spec 24.4.17. Complete file.
// Change from 24.4.14: CarsViewModel registered (CarsScreen resolves it
// via koinViewModel).
package com.icodeforyou.catscarscoins.cars.di

import com.icodeforyou.catscarscoins.cars.data.CarApi
import com.icodeforyou.catscarscoins.cars.data.CarApiRemoteSource
import com.icodeforyou.catscarscoins.cars.data.RoomCarsRepository
import com.icodeforyou.catscarscoins.cars.domain.CarsRemoteSource
import com.icodeforyou.catscarscoins.cars.domain.CarsRepository
import com.icodeforyou.catscarscoins.cars.ui.CarsViewModel
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** Converter media type — fixed by HTTP/JSON standards per call site. */
private const val JSON_MEDIA_TYPE = "application/json"

/**
 * Cars feature wiring (spec: Koin runtime DSL). Consumers inject domain
 * contracts; Room, Retrofit, and wire types stay behind this file (DIP).
 * CarDao resolves from databaseModule; OkHttpClient and Json from
 * networkModule.
 *
 * Deliberate contrast with catsModule: vPIC is keyless, so the shared
 * OkHttpClient is used as-is — no newBuilder(), no interceptor.
 */
val carsModule = module {

    single<CarsRepository> {
        RoomCarsRepository(
            carDao = get(),
            remoteSource = get(),
        )
    }

    single<CarApi> {
        Retrofit.Builder()
            .baseUrl(CarApi.BASE_URL)
            .client(get<OkHttpClient>())
            .addConverterFactory(
                get<Json>().asConverterFactory(JSON_MEDIA_TYPE.toMediaType()),
            )
            .build()
            .create(CarApi::class.java)
    }

    single<CarsRemoteSource> {
        CarApiRemoteSource(api = get())
    }

    viewModelOf(::CarsViewModel)
}