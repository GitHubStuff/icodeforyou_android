// coins/ui/CoinsViewModelTest.kt
// CatsCarsCoins — spec 24.2.29. Complete file. Test sources.
package com.icodeforyou.catscarscoins.coins.ui

import app.cash.turbine.test
import com.icodeforyou.catscarscoins.coins.domain.Coin
import com.icodeforyou.catscarscoins.coins.domain.CoinRefresher
import com.icodeforyou.catscarscoins.coins.domain.CoinsRepository
import com.icodeforyou.catscarscoins.testsupport.MainDispatcherRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CoinsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class EmittingCoinsRepository : CoinsRepository {

        val state = MutableStateFlow<List<Coin>>(emptyList())

        override val coins: Flow<List<Coin>> = state

        override suspend fun record(amountCents: Long, recordedAtEpochMillis: Long) {
            error("not exercised by these tests")
        }

        override suspend fun clearAll() {
            state.value = emptyList()
        }
    }

    private class RecordingRefresher : CoinRefresher {

        var refreshCount = 0

        override fun refresh() {
            refreshCount++
        }
    }

    private fun coin(id: Long, amountCents: Long): Coin = Coin(
        id = id,
        amountCents = amountCents,
        recordedAtEpochMillis = id,
    )

    @Test
    fun `initial list is empty`() = runTest {
        val viewModel = CoinsViewModel(EmittingCoinsRepository(), RecordingRefresher())

        assertTrue(viewModel.coins.value.isEmpty())
    }

    @Test
    fun `repository emissions reach the coins state`() = runTest {
        val repository = EmittingCoinsRepository()
        val viewModel = CoinsViewModel(repository, RecordingRefresher())

        viewModel.coins.test {
            assertTrue(awaitItem().isEmpty())

            val samples = listOf(coin(id = 2L, amountCents = 200L), coin(id = 1L, amountCents = 100L))
            repository.state.value = samples
            assertEquals(samples, awaitItem())
        }
    }

    @Test
    fun `onRefresh delegates to the refresher`() = runTest {
        val refresher = RecordingRefresher()
        val viewModel = CoinsViewModel(EmittingCoinsRepository(), refresher)

        viewModel.onRefresh()
        viewModel.onRefresh()

        assertEquals(2, refresher.refreshCount)
    }
}