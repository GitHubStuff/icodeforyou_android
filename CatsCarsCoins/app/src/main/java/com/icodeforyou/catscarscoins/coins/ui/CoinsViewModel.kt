// coins/ui/CoinsViewModel.kt
// CatsCarsCoins — spec 24.2.31. Complete file.
package com.icodeforyou.catscarscoins.coins.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icodeforyou.catscarscoins.coins.domain.Coin
import com.icodeforyou.catscarscoins.coins.domain.CoinRefresher
import com.icodeforyou.catscarscoins.coins.domain.CoinsRepository
import com.icodeforyou.catscarscoins.ui.SUBSCRIPTION_STOP_TIMEOUT_MS
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Coins list state (Phase 2 UI). One state, one action: the stored
 * samples newest-first (ordering is the repository's guarantee), and
 * [onRefresh] delegating to the [CoinRefresher] seam — §19 semantics
 * (poll now, reset timer, never touch pause) live in the engine, not
 * here.
 */
class CoinsViewModel(
    coinsRepository: CoinsRepository,
    private val coinRefresher: CoinRefresher,
) : ViewModel() {

    val coins: StateFlow<List<Coin>> = coinsRepository.coins
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_STOP_TIMEOUT_MS),
            initialValue = emptyList(),
        )

    fun onRefresh() {
        coinRefresher.refresh()
    }
}