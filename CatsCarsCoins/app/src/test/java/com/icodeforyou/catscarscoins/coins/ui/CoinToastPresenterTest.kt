// coins/ui/CoinToastPresenterTest.kt
// CatsCarsCoins — spec 24.2.48. Complete file. Test sources.
package com.icodeforyou.catscarscoins.coins.ui

import com.icodeforyou.catscarscoins.coins.domain.Coin
import com.icodeforyou.catscarscoins.coins.domain.FakeCoinsRepository
import com.icodeforyou.catscarscoins.notifier.COIN_TOAST_MILLIS
import com.icodeforyou.catscarscoins.notifier.Notifier
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CoinToastPresenterTest {

    private fun coin(id: Long, amountCents: Long): Coin = Coin(
        id = id,
        amountCents = amountCents,
        recordedAtEpochMillis = id,
    )

    private fun TestScope.startPresenter(
        repository: FakeCoinsRepository,
        notifier: Notifier,
    ) {
        CoinToastPresenter(
            coinsRepository = repository,
            notifier = notifier,
            scope = backgroundScope,
        ).start()
        runCurrent()
    }

    @Test
    fun `initial emission with existing samples does not toast`() = runTest {
        val notifier = Notifier()
        startPresenter(
            FakeCoinsRepository(initial = listOf(coin(id = 1L, amountCents = 100L))),
            notifier,
        )

        assertNull(notifier.current.value)
    }

    @Test
    fun `initial emission with an empty table does not toast`() = runTest {
        val notifier = Notifier()
        startPresenter(FakeCoinsRepository(), notifier)

        assertNull(notifier.current.value)
    }

    @Test
    fun `first sample into an empty table toasts`() = runTest {
        val repository = FakeCoinsRepository()
        val notifier = Notifier()
        startPresenter(repository, notifier)

        repository.record(amountCents = 100L, recordedAtEpochMillis = 1L)
        runCurrent()

        assertNotNull(notifier.current.value)
        assertEquals(COIN_TOAST_MILLIS, notifier.current.value?.durationMillis)
    }

    @Test
    fun `a new head after existing samples toasts`() = runTest {
        val repository = FakeCoinsRepository(
            initial = listOf(coin(id = 1L, amountCents = 100L)),
        )
        val notifier = Notifier()
        startPresenter(repository, notifier)

        repository.record(amountCents = 200L, recordedAtEpochMillis = 2L)
        runCurrent()

        assertNotNull(notifier.current.value)
        assertEquals(COIN_TOAST_MILLIS, notifier.current.value?.durationMillis)
    }

    @Test
    fun `each new head produces a fresh toast`() = runTest {
        val repository = FakeCoinsRepository()
        val notifier = Notifier()
        startPresenter(repository, notifier)

        repository.record(amountCents = 100L, recordedAtEpochMillis = 1L)
        runCurrent()
        val first = notifier.current.value

        repository.record(amountCents = 200L, recordedAtEpochMillis = 2L)
        runCurrent()
        val second = notifier.current.value

        assertNotNull(second)
        assertNotEquals(first?.id, second?.id)
    }

    @Test
    fun `same-head re-emission does not re-toast`() = runTest {
        val repository = FakeCoinsRepository()
        val notifier = Notifier()
        startPresenter(repository, notifier)

        repository.record(amountCents = 100L, recordedAtEpochMillis = 1L)
        repository.record(amountCents = 200L, recordedAtEpochMillis = 2L)
        runCurrent()
        notifier.dismiss()

        val trimmedTailSameHead = listOf(repository.state.value.first())
        repository.state.value = trimmedTailSameHead
        runCurrent()

        assertNull(notifier.current.value)
    }

    @Test
    fun `a reused id after the table empties still toasts`() = runTest {
        val repository = FakeCoinsRepository(
            initial = listOf(coin(id = 1L, amountCents = 100L)),
        )
        val notifier = Notifier()
        startPresenter(repository, notifier)

        repository.clearAll()
        runCurrent()
        assertNull(notifier.current.value)

        repository.state.value = listOf(coin(id = 1L, amountCents = 300L))
        runCurrent()

        assertNotNull(notifier.current.value)
    }
}