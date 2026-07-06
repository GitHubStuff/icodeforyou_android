// coins/domain/CoinPollingEngineTest.kt
// CatsCarsCoins — spec 24.2.22. Complete file. Test sources.
// Correction folded in: TestScope.currentTime is an extension property and
// needs its own import (kotlinx.coroutines.test.currentTime).
package com.icodeforyou.catscarscoins.coins.domain

import com.icodeforyou.catscarscoins.preferences.domain.AppPreferences
import com.icodeforyou.catscarscoins.preferences.domain.FakePreferencesRepository
import java.io.IOException
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CoinPollingEngineTest {

    private class FakeCoinPriceSource : CoinPriceSource {

        var nextAmountCents: Long = 100L
        var failNextFetch: Boolean = false

        override suspend fun currentAmountCents(): Long {
            if (failNextFetch) {
                failNextFetch = false
                throw IOException("fake network failure")
            }
            return nextAmountCents
        }
    }

    private class RecordingCoinsRepository : CoinsRepository {

        val recordedAmounts = mutableListOf<Long>()

        override val coins: Flow<List<Coin>> = flowOf(emptyList())

        override suspend fun record(amountCents: Long, recordedAtEpochMillis: Long) {
            recordedAmounts.add(amountCents)
        }

        override suspend fun clearAll() {
            recordedAmounts.clear()
        }
    }

    private val unpausedPreferences = AppPreferences.DEFAULTS.copy(pollingPaused = false)

    private fun TestScope.startEngine(
        source: FakeCoinPriceSource,
        repository: RecordingCoinsRepository,
        preferences: FakePreferencesRepository,
    ): CoinPollingEngine {
        val engine = CoinPollingEngine(
            priceSource = source,
            coinsRepository = repository,
            preferencesRepository = preferences,
            scope = backgroundScope,
            recordedAtProvider = { currentTime },
        )
        engine.start()
        runCurrent()
        return engine
    }

    @Test
    fun `default preferences poll nothing because pause defaults to true`() = runTest {
        val repository = RecordingCoinsRepository()
        startEngine(FakeCoinPriceSource(), repository, FakePreferencesRepository())

        advanceTimeBy(60.seconds)
        runCurrent()

        assertEquals(emptyList<Long>(), repository.recordedAmounts)
    }

    @Test
    fun `unpaused engine polls immediately on start`() = runTest {
        val repository = RecordingCoinsRepository()
        startEngine(
            FakeCoinPriceSource().apply { nextAmountCents = 100L },
            repository,
            FakePreferencesRepository(unpausedPreferences),
        )

        assertEquals(listOf(100L), repository.recordedAmounts)
    }

    @Test
    fun `polls at the configured interval`() = runTest {
        val source = FakeCoinPriceSource().apply { nextAmountCents = 100L }
        val repository = RecordingCoinsRepository()
        startEngine(source, repository, FakePreferencesRepository(unpausedPreferences))

        source.nextAmountCents = 200L
        advanceTimeBy(5.seconds)
        runCurrent()
        source.nextAmountCents = 300L
        advanceTimeBy(5.seconds)
        runCurrent()

        assertEquals(listOf(100L, 200L, 300L), repository.recordedAmounts)
    }

    @Test
    fun `consecutive duplicate amounts record once`() = runTest {
        val repository = RecordingCoinsRepository()
        startEngine(
            FakeCoinPriceSource().apply { nextAmountCents = 100L },
            repository,
            FakePreferencesRepository(unpausedPreferences),
        )

        advanceTimeBy(15.seconds)
        runCurrent()

        assertEquals(listOf(100L), repository.recordedAmounts)
    }

    @Test
    fun `non-consecutive repeat of an amount records again`() = runTest {
        val source = FakeCoinPriceSource().apply { nextAmountCents = 100L }
        val repository = RecordingCoinsRepository()
        startEngine(source, repository, FakePreferencesRepository(unpausedPreferences))

        source.nextAmountCents = 200L
        advanceTimeBy(5.seconds)
        runCurrent()
        source.nextAmountCents = 100L
        advanceTimeBy(5.seconds)
        runCurrent()

        assertEquals(listOf(100L, 200L, 100L), repository.recordedAmounts)
    }

    @Test
    fun `pausing stops the poll timer`() = runTest {
        val source = FakeCoinPriceSource().apply { nextAmountCents = 100L }
        val repository = RecordingCoinsRepository()
        val preferences = FakePreferencesRepository(unpausedPreferences)
        startEngine(source, repository, preferences)

        preferences.setPollingPaused(true)
        runCurrent()
        source.nextAmountCents = 200L
        advanceTimeBy(60.seconds)
        runCurrent()

        assertEquals(listOf(100L), repository.recordedAmounts)
    }

    @Test
    fun `unpausing polls immediately`() = runTest {
        val repository = RecordingCoinsRepository()
        val preferences = FakePreferencesRepository()
        startEngine(
            FakeCoinPriceSource().apply { nextAmountCents = 100L },
            repository,
            preferences,
        )

        preferences.setPollingPaused(false)
        runCurrent()

        assertEquals(listOf(100L), repository.recordedAmounts)
    }

    @Test
    fun `refresh polls immediately even while paused`() = runTest {
        val repository = RecordingCoinsRepository()
        val engine = startEngine(
            FakeCoinPriceSource().apply { nextAmountCents = 100L },
            repository,
            FakePreferencesRepository(),
        )

        engine.refresh()
        runCurrent()

        assertEquals(listOf(100L), repository.recordedAmounts)
    }

    @Test
    fun `refresh resets the next poll timer`() = runTest {
        val source = FakeCoinPriceSource().apply { nextAmountCents = 100L }
        val repository = RecordingCoinsRepository()
        val engine = startEngine(source, repository, FakePreferencesRepository(unpausedPreferences))

        advanceTimeBy(3.seconds)
        runCurrent()
        source.nextAmountCents = 200L
        engine.refresh()
        runCurrent()
        assertEquals(listOf(100L, 200L), repository.recordedAmounts)

        source.nextAmountCents = 300L
        advanceTimeBy(4.seconds)
        runCurrent()
        assertEquals(listOf(100L, 200L), repository.recordedAmounts)

        advanceTimeBy(1.seconds)
        runCurrent()
        assertEquals(listOf(100L, 200L, 300L), repository.recordedAmounts)
    }

    @Test
    fun `interval change restarts the cycle with an immediate poll`() = runTest {
        val source = FakeCoinPriceSource().apply { nextAmountCents = 100L }
        val repository = RecordingCoinsRepository()
        val preferences = FakePreferencesRepository(unpausedPreferences)
        startEngine(source, repository, preferences)

        source.nextAmountCents = 200L
        preferences.setPollingIntervalSeconds(10)
        runCurrent()
        assertEquals(listOf(100L, 200L), repository.recordedAmounts)

        source.nextAmountCents = 300L
        advanceTimeBy(5.seconds)
        runCurrent()
        assertEquals(listOf(100L, 200L), repository.recordedAmounts)

        advanceTimeBy(5.seconds)
        runCurrent()
        assertEquals(listOf(100L, 200L, 300L), repository.recordedAmounts)
    }

    @Test
    fun `failed fetch skips the tick and polling continues`() = runTest {
        val source = FakeCoinPriceSource().apply {
            nextAmountCents = 100L
            failNextFetch = true
        }
        val repository = RecordingCoinsRepository()
        startEngine(source, repository, FakePreferencesRepository(unpausedPreferences))

        assertEquals(emptyList<Long>(), repository.recordedAmounts)

        advanceTimeBy(5.seconds)
        runCurrent()

        assertEquals(listOf(100L), repository.recordedAmounts)
    }
}