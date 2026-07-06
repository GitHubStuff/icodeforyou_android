// cats/data/RoomCatsRepositoryTest.kt
// CatsCarsCoins — spec 24.3.9. Complete file. androidTest sources.
package com.icodeforyou.catscarscoins.cats.data

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.icodeforyou.catscarscoins.cats.domain.Breed
import com.icodeforyou.catscarscoins.cats.domain.Cat
import com.icodeforyou.catscarscoins.cats.domain.CatsRemoteSource
import com.icodeforyou.catscarscoins.db.CatsCarsDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomCatsRepositoryTest {

    private class FakeCatsRemoteSource : CatsRemoteSource {

        var page: List<Cat> = emptyList()

        override suspend fun fetchPage(limit: Int): List<Cat> = page
    }

    private lateinit var database: CatsCarsDatabase
    private lateinit var remoteSource: FakeCatsRemoteSource
    private lateinit var repository: RoomCatsRepository

    private val bengal = cat(
        id = "b1",
        breedName = "Bengal",
        origin = "United States",
        temperament = "Alert, Agile, Energetic",
        description = "Bengals are a lot of fun to live with",
    )
    private val persian = cat(
        id = "p1",
        breedName = "Persian",
        origin = "Iran",
        temperament = "Quiet, Sweet",
        description = "The quintessential calm lap cat",
    )
    private val siamese = cat(
        id = "s1",
        breedName = "Siamese",
        origin = "Thailand",
        temperament = "Social, Vocal",
        description = "Talkative and people-oriented",
    )

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder<CatsCarsDatabase>(context)
            .setDriver(BundledSQLiteDriver())
            .build()
        remoteSource = FakeCatsRemoteSource()
        repository = RoomCatsRepository(
            catDao = database.catDao(),
            remoteSource = remoteSource,
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun cat(
        id: String,
        breedName: String,
        origin: String,
        temperament: String,
        description: String,
    ): Cat = Cat(
        id = id,
        imageUrl = "https://example.test/$id.jpg",
        breed = Breed(
            id = "breed-$id",
            name = breedName,
            origin = origin,
            temperament = temperament,
            lifeSpan = "12 - 15",
            description = description,
        ),
    )

    private suspend fun seedAllThree() {
        remoteSource.page = listOf(siamese, bengal, persian)
        repository.refresh()
    }

    @Test
    fun blankQueryReturnsAllAlphabeticallyByBreed() = runTest {
        seedAllThree()

        repository.cats("").test {
            val names = awaitItem().map { it.breed.name }
            assertEquals(listOf("Bengal", "Persian", "Siamese"), names)
        }
    }

    @Test
    fun matchIsCaseInsensitive() = runTest {
        seedAllThree()

        repository.cats("bengal").test {
            assertEquals(listOf("b1"), awaitItem().map { it.id })
        }
    }

    @Test
    fun prefixMatches() = runTest {
        seedAllThree()

        repository.cats("agi").test {
            assertEquals(listOf("b1"), awaitItem().map { it.id })
        }
    }

    @Test
    fun multiWordQueryRequiresAllTokens() = runTest {
        seedAllThree()

        repository.cats("alert agile").test {
            assertEquals(listOf("b1"), awaitItem().map { it.id })
        }
    }

    @Test
    fun originAndDescriptionFieldsAreSearchable() = runTest {
        seedAllThree()

        repository.cats("thailand").test {
            assertEquals(listOf("s1"), awaitItem().map { it.id })
        }
        repository.cats("lap").test {
            assertEquals(listOf("p1"), awaitItem().map { it.id })
        }
    }

    @Test
    fun noMatchReturnsEmpty() = runTest {
        seedAllThree()

        repository.cats("zebra").test {
            assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun refreshTwiceWithSamePageDoesNotDuplicate() = runTest {
        seedAllThree()
        seedAllThree()

        repository.cats("").test {
            assertEquals(3, awaitItem().size)
        }
    }

    @Test
    fun refreshUpdatesChangedDataForSameId() = runTest {
        seedAllThree()
        remoteSource.page = listOf(
            bengal.copy(imageUrl = "https://example.test/b1-new.jpg"),
        )

        repository.refresh()

        repository.cats("bengal").test {
            assertEquals("https://example.test/b1-new.jpg", awaitItem().first().imageUrl)
        }
    }

    @Test
    fun clearAllEmptiesContentAndIndex() = runTest {
        seedAllThree()

        repository.clearAll()

        repository.cats("").test {
            assertTrue(awaitItem().isEmpty())
        }
        repository.cats("bengal").test {
            assertTrue(awaitItem().isEmpty())
        }
    }
}