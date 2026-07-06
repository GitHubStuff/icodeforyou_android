// cars/data/RoomCarsRepositoryTest.kt
// CatsCarsCoins — spec 24.4.8. Complete file. androidTest sources.
package com.icodeforyou.catscarscoins.cars.data

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.icodeforyou.catscarscoins.cars.domain.CarsRemoteSource
import com.icodeforyou.catscarscoins.cars.domain.Manufacturer
import com.icodeforyou.catscarscoins.db.CatsCarsDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomCarsRepositoryTest {

    private class FakeCarsRemoteSource : CarsRemoteSource {
        var page: List<Manufacturer> = emptyList()
        override suspend fun fetchPage(page: Int): List<Manufacturer> = this.page
    }

    private lateinit var database: CatsCarsDatabase
    private lateinit var remoteSource: FakeCarsRemoteSource
    private lateinit var repository: RoomCarsRepository

    private val aston = Manufacturer(id = 1, name = "Aston Martin", country = "UNITED KINGDOM (UK)")
    private val bmw = Manufacturer(id = 2, name = "BMW AG", country = "GERMANY")
    private val tesla = Manufacturer(id = 3, name = "Tesla, Inc.", country = "UNITED STATES (USA)")

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder<CatsCarsDatabase>(context)
            .setDriver(BundledSQLiteDriver())
            .build()
        remoteSource = FakeCarsRemoteSource()
        repository = RoomCarsRepository(
            carDao = database.carDao(),
            remoteSource = remoteSource,
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    private suspend fun seedAllThree() {
        remoteSource.page = listOf(tesla, aston, bmw)
        repository.refresh()
    }

    @Test
    fun blankQueryReturnsAllAlphabeticallyByName() = runTest {
        seedAllThree()

        repository.manufacturers("").test {
            val names = awaitItem().map { it.name }
            assertEquals(listOf("Aston Martin", "BMW AG", "Tesla, Inc."), names)
        }
    }

    @Test
    fun matchIsCaseInsensitive() = runTest {
        seedAllThree()

        repository.manufacturers("bmw").test {
            assertEquals(listOf(2), awaitItem().map { it.id })
        }
    }

    @Test
    fun prefixMatches() = runTest {
        seedAllThree()

        repository.manufacturers("tes").test {
            assertEquals(listOf(3), awaitItem().map { it.id })
        }
    }

    @Test
    fun nameAndCountryFieldsAreSearchable() = runTest {
        seedAllThree()

        repository.manufacturers("aston").test {
            assertEquals(listOf(1), awaitItem().map { it.id })
        }
        repository.manufacturers("germany").test {
            assertEquals(listOf(2), awaitItem().map { it.id })
        }
    }

    @Test
    fun multiWordQueryRequiresAllTokens() = runTest {
        seedAllThree()

        repository.manufacturers("united kingdom").test {
            assertEquals(listOf(1), awaitItem().map { it.id })
        }
    }

    @Test
    fun noMatchReturnsEmpty() = runTest {
        seedAllThree()

        repository.manufacturers("zebra").test {
            assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun refreshTwiceWithSamePageDoesNotDuplicate() = runTest {
        seedAllThree()
        seedAllThree()

        repository.manufacturers("").test {
            assertEquals(3, awaitItem().size)
        }
    }

    @Test
    fun refreshUpdatesChangedDataForSameId() = runTest {
        seedAllThree()
        remoteSource.page = listOf(tesla.copy(country = "MARS"))

        repository.refresh()

        repository.manufacturers("tesla").test {
            assertEquals("MARS", awaitItem().first().country)
        }
    }

    @Test
    fun clearAllEmptiesContentAndIndex() = runTest {
        seedAllThree()

        repository.clearAll()

        repository.manufacturers("").test {
            assertTrue(awaitItem().isEmpty())
        }
        repository.manufacturers("bmw").test {
            assertTrue(awaitItem().isEmpty())
        }
    }
}