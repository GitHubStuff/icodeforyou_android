// ui/icons/MushroomCloud.kt
// CatsCarsCoins — Complete file.
// Hand-authored ImageVector (no Material mushroom-cloud icon exists):
// billowing cap, narrow stem, ground cloud, in the standard 24x24
// viewport. Fill color is irrelevant — Icon() tints it with
// LocalContentColor like any built-in icon.
package com.icodeforyou.catscarscoins.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Stylized mushroom cloud, drawn once and cached. */
val MushroomCloud: ImageVector by lazy {
    ImageVector.Builder(
        name = "MushroomCloud",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            // Cap — wide billowing dome.
            moveTo(12f, 2f)
            curveTo(7f, 2f, 4f, 4.5f, 4f, 7f)
            curveTo(4f, 9f, 6f, 10.6f, 8.5f, 10.9f)
            lineTo(15.5f, 10.9f)
            curveTo(18f, 10.6f, 20f, 9f, 20f, 7f)
            curveTo(20f, 4.5f, 17f, 2f, 12f, 2f)
            close()
            // Stem — narrow column flaring toward the ground.
            moveTo(10.4f, 10.9f)
            lineTo(9.2f, 17f)
            lineTo(14.8f, 17f)
            lineTo(13.6f, 10.9f)
            close()
            // Ground cloud — the low ring at the base.
            moveTo(9.2f, 17f)
            curveTo(7.4f, 17f, 6f, 18.3f, 6f, 20f)
            curveTo(6f, 21.1f, 6.9f, 22f, 8f, 22f)
            lineTo(16f, 22f)
            curveTo(17.1f, 22f, 18f, 21.1f, 18f, 20f)
            curveTo(18f, 18.3f, 16.6f, 17f, 14.8f, 17f)
            close()
        }
    }.build()
}