// dbviewer/nav/DbViewerNavKeys.kt
// CatsCarsCoins — spec 24.5.14. Complete file.
package com.icodeforyou.catscarscoins.dbviewer.nav

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * DbViewer feature key (feature keys live in the feature's own nav
 * package — the CatDetailKey convention). Pushed on top of Settings by
 * the Database Tools card; system back pops to Settings. @Serializable
 * so the developer-owned back stack survives process death.
 */
@Serializable
data object DbViewerKey : NavKey