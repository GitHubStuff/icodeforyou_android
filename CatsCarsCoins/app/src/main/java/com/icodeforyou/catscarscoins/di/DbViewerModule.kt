// di/DbViewerModule.kt
// CatsCarsCoins — spec 24.5.14. Complete file.
// Change from Gemini baseline: ResetAppUseCase now binds the domain
// contract to the relocated data implementation; KDoc normalized.
package com.icodeforyou.catscarscoins.di

import com.icodeforyou.catscarscoins.db.CatsCarsDatabase
import com.icodeforyou.catscarscoins.dbviewer.data.RoomDbViewerRepository
import com.icodeforyou.catscarscoins.dbviewer.data.RoomResetAppUseCase
import com.icodeforyou.catscarscoins.dbviewer.domain.DbViewerRepository
import com.icodeforyou.catscarscoins.dbviewer.domain.ResetAppUseCase
import com.icodeforyou.catscarscoins.dbviewer.ui.DbViewerViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * DbViewer feature wiring (spec: Koin runtime DSL). Consumers inject the
 * domain contracts; Room connection plumbing stays behind this file.
 *
 * Documented exception to the DatabaseModule rule ("features inject
 * exactly the DAO they own"): the viewer is schemaless BY DESIGN — it
 * reads sqlite_master and raw rows, so it needs the database itself,
 * not a DAO. No other feature may copy this.
 */
val dbViewerModule: Module = module {

    single<DbViewerRepository> {
        RoomDbViewerRepository(database = get<CatsCarsDatabase>())
    }

    factory<ResetAppUseCase> {
        RoomResetAppUseCase(database = get<CatsCarsDatabase>())
    }

    viewModelOf(::DbViewerViewModel)
}