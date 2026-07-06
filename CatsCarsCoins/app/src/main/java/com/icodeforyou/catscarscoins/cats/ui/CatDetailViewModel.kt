// cats/ui/CatDetailViewModel.kt
// CatsCarsCoins — spec 24.3.30. Complete file.
package com.icodeforyou.catscarscoins.cats.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icodeforyou.catscarscoins.cats.domain.Cat
import com.icodeforyou.catscarscoins.cats.domain.CatsRepository
import com.icodeforyou.catscarscoins.ui.SUBSCRIPTION_STOP_TIMEOUT_MS
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Cat detail state (Phase 3 UI). Selects the [catId] cat from the
 * repository's already-cached list — the one-call has_breeds=1 design
 * means detail needs no second fetch. Emits null when the id isn't present
 * (stale key → not-found screen). Observing (not snapshotting) keeps the
 * detail live if a background refresh updates the selected cat.
 */
class CatDetailViewModel(
    private val catId: String,
    catsRepository: CatsRepository,
) : ViewModel() {

    val cat: StateFlow<Cat?> = catsRepository.cats("")
        .map { cats -> cats.firstOrNull { it.id == catId } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_STOP_TIMEOUT_MS),
            initialValue = null,
        )
}