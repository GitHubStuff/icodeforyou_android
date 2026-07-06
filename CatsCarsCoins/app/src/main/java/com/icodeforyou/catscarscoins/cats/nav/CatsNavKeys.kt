// cats/nav/CatsNavKeys.kt
// CatsCarsCoins — spec 24.3.26. Complete file.
package com.icodeforyou.catscarscoins.cats.nav

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Cats feature navigation keys (per the AppNavKeys rule: feature keys
 * live in the feature's own nav/ package; the app-shell sealed interface
 * covers only top-level destinations).
 *
 * [CatDetailKey] is the app's first parameterized key: the payload is the
 * remote cat id, @Serializable like every key so the developer-owned back
 * stack — detail included — survives process death. Detail is pushed ON
 * TOP of CatsKey (backStack.add), so system back pops naturally to the
 * list; it is not a rail destination.
 */
@Serializable
data class CatDetailKey(
    val catId: String,
) : NavKey