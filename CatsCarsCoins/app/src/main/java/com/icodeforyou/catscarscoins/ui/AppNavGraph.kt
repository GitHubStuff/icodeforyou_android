// catscarscoins/ui/AppNavGraph.kt
package com.icodeforyou.catscarscoins.ui

import androidx.compose.runtime.Composable
import com.icodeforyou.catscarscoins.cars.ui.CarsScreen
import com.icodeforyou.catscarscoins.cats.ui.CatsScreen
import com.icodeforyou.catscarscoins.coins.ui.CoinsScreen
import com.icodeforyou.catscarscoins.dbviewer.ui.DbViewerScreen

/**
 * Global navigation routing switcher managing top-level feature screens.
 */
@Composable
fun AppNavGraph(
    currentScreen: String,
    onCatSelected: (String) -> Unit
) {
    when (currentScreen) {
        "coins" -> {
            CoinsScreen()
        }
        "cats" -> {
            CatsScreen(onCatSelected = onCatSelected)
        }
        "cars" -> {
            CarsScreen()
        }
        "db_viewer" -> {
            DbViewerScreen()
        }
    }
}