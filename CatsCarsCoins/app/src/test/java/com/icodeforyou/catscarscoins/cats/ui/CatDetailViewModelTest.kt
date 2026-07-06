// cats/ui/CatDetailViewModelTest.kt
// CatsCarsCoins — spec 24.3.29. Complete file. Test sources.
package com.icodeforyou.catscarscoins.cats.ui

import app.cash.turbine.test
import com.icodeforyou.catscarscoins.cats.domain.Breed
import com.icodeforyou.catscarscoins.cats.domain.Cat
import com.icodeforyou.catscarscoins.cats.domain.FakeCatsRepository
import com.icodeforyou.catscarscoins.testsupport.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class CatDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun cat(id: String, breedName: String): Cat = Cat(
        id = id,
        imageUrl = "https://example.test/$id.jpg",
        breed = Breed(
            id = "breed-$id",
            name = breedName,
            origin = "Origin-$id",
            temperament = "Temperament-$id",
            lifeSpan = "12 - 15",
            description = "Description-$id",
        ),
    )

    private val bengal = cat(id = "b1", breedName = "Bengal")
    private val persian = cat(id = "p1", breedName = "Persian")

    @Test
    fun `selects the cat matching the id`() = runTest {
        val repository = FakeCatsRepository(initial = listOf(bengal, persian))
        val viewModel = CatDetailViewModel(catId = "p1", catsRepository = repository)

        viewModel.cat.test {
            assertEquals(persian, awaitItem())
        }
    }

    @Test
    fun `emits null when no cat matches the id`() = runTest {
        val repository = FakeCatsRepository(initial = listOf(bengal))
        val viewModel = CatDetailViewModel(catId = "missing", catsRepository = repository)

        viewModel.cat.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `reflects repository updates for the selected cat`() = runTest {
        val repository = FakeCatsRepository(initial = listOf(bengal))
        val viewModel = CatDetailViewModel(catId = "b1", catsRepository = repository)

        viewModel.cat.test {
            assertEquals(bengal, awaitItem())

            val updated = bengal.copy(imageUrl = "https://example.test/b1-new.jpg")
            repository.state.value = listOf(updated)
            assertEquals(updated, awaitItem())
        }
    }
}