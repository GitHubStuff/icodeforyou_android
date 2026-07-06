// coins/data/RoomCoinsRepositoryTest.kt
// CatsCarsCoins — spec 24.2.10. Complete file. androidTest sources.
package com.icodeforyou.catscarscoins.coins.data

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.icodeforyou.catscarscoins.coins.domain.Coin
import com.icodeforyou.catscarscoins.db.CatsCarsDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomCoinsRepositoryTest {

    private lateinit var database: CatsCarsDatabase
    private lateinit var repository: RoomCoinsRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder<CatsCarsDatabase>(context)
            .setDriver(BundledSQLiteDriver())
            .build()
        repository = RoomCoinsRepository(database.coinDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun recordedSampleIsObservedWithAssignedId() = runTest {
        repository.record(amountCents = 434317L, recordedAtEpochMillis = 1_000L)

        repository.coins.test {
            val coins = awaitItem()
            assertEquals(1, coins.size)
            assertEquals(434317L, coins.first().amountCents)
            assertEquals(1_000L, coins.first().recordedAtEpochMillis)
            assertTrue(coins.first().id > Coin.UNSAVED_ID)
        }
    }

    @Test
    fun coinsEmitNewestFirst() = runTest {
        repository.record(amountCents = 100L, recordedAtEpochMillis = 1_000L)
        repository.record(amountCents = 200L, recordedAtEpochMillis = 2_000L)
        repository.record(amountCents = 300L, recordedAtEpochMillis = 3_000L)

        repository.coins.test {
            val amounts = awaitItem().map { it.amountCents }
            assertEquals(listOf(300L, 200L, 100L), amounts)
        }
    }

    @Test
    fun sameMillisecondSamplesOrderByIdDescending() = runTest {
        repository.record(amountCents = 100L, recordedAtEpochMillis = 1_000L)
        repository.record(amountCents = 200L, recordedAtEpochMillis = 1_000L)

        repository.coins.test {
            val amounts = awaitItem().map { it.amountCents }
            assertEquals(listOf(200L, 100L), amounts)
        }
    }

    @Test
    fun recordTrimsOldestBeyondRowCap() = runTest {
        repeat(Coin.ROW_CAP + 1) { index ->
            repository.record(
                amountCents = index.toLong(),
                recordedAtEpochMillis = index.toLong(),
            )
        }

        repository.coins.test {
            val coins = awaitItem()
            assertEquals(Coin.ROW_CAP, coins.size)
            assertEquals(Coin.ROW_CAP.toLong(), coins.first().amountCents)
            assertEquals(1L, coins.last().amountCents)
        }
    }

    @Test
    fun clearAllEmptiesTheTable() = runTest {
        repository.record(amountCents = 100L, recordedAtEpochMillis = 1_000L)
        repository.record(amountCents = 200L, recordedAtEpochMillis = 2_000L)

        repository.clearAll()

        repository.coins.test {
            assertTrue(awaitItem().isEmpty())
        }
    }
}