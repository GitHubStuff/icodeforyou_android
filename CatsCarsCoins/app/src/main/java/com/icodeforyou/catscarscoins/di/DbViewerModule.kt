// catscarscoins/di/DbViewerModule.kt
package com.icodeforyou.catscarscoins.di

import com.icodeforyou.catscarscoins.db.CatsCarsDatabase
import com.icodeforyou.catscarscoins.dbviewer.data.RoomDbViewerRepository
import com.icodeforyou.catscarscoins.dbviewer.domain.DbViewerRepository
import com.icodeforyou.catscarscoins.dbviewer.domain.ResetAppUseCase
import com.icodeforyou.catscarscoins.dbviewer.ui.DbViewerViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Dependency injection graph slice for the Phase 5 Database Viewer feature.
 * Binds the Room-backed schema reader, registers destruction utilities, and binds the presentation model.
 */
val dbViewerModule: Module = module {
    single<DbViewerRepository> {
        RoomDbViewerRepository(database = get<CatsCarsDatabase>())
    }

    factory {
        ResetAppUseCase(database = get<CatsCarsDatabase>())
    }

    viewModelOf(::DbViewerViewModel)
}