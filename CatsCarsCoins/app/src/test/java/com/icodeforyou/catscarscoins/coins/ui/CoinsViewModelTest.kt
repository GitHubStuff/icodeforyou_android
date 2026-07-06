// coins/ui/CoinsViewModelTest.kt
// CatsCarsCoins — spec 24.2.47. Complete file.
// Change from 24.2.29: nested EmittingCoinsRepository deleted — replaced
// by the shared FakeCoinsRepository (24.2.46). Tests and assertions
// unchanged; RecordingRefresher stays nested (single user).
package com.icodeforyou.catscarscoins.coins.ui

import app.cash.turbine.test
import com.icodeforyou.catscarscoins.coins.domain.Coin
import com.icodeforyou.catscarscoins.coins.domain.CoinRefresher
import com.icodeforyou.catscarscoins.coins.domain.FakeCoinsRepository
import com.icodeforyou.catscarscoins.testsupport.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CoinsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

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
        val viewModel = CoinsViewModel(FakeCoinsRepository(), RecordingRefresher())

        assertTrue(viewModel.coins.value.isEmpty())
    }

    @Test
    fun `repository emissions reach the coins state`() = runTest {
        val repository = FakeCoinsRepository()
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
        val viewModel = CoinsViewModel(FakeCoinsRepository(), refresher)

        viewModel.onRefresh()
        viewModel.onRefresh()

        assertEquals(2, refresher.refreshCount)
    }
}