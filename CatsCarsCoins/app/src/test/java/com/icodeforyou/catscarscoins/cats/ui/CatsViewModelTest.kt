// cats/ui/CatsViewModelTest.kt
// CatsCarsCoins — spec 24.3.18. Complete file. Test sources.
package com.icodeforyou.catscarscoins.cats.ui

import app.cash.turbine.test
import com.icodeforyou.catscarscoins.cats.domain.Breed
import com.icodeforyou.catscarscoins.cats.domain.Cat
import com.icodeforyou.catscarscoins.cats.domain.CatsRepository
import com.icodeforyou.catscarscoins.testsupport.MainDispatcherRule
import java.io.IOException
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CatsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakeCatsRepository : CatsRepository {

        val state = MutableStateFlow<List<Cat>>(emptyList())
        val queriesSeen = mutableListOf<String>()
        var refreshCount = 0
        var failNextRefresh = false

        override fun cats(query: String): Flow<List<Cat>> {
            queriesSeen.add(query)
            return state.map { cats ->
                if (query.isBlank()) {
                    cats
                } else {
                    cats.filter { cat ->
                        cat.breed.name.contains(query, ignoreCase = true)
                    }
                }
            }
        }

        override suspend fun refresh() {
            refreshCount++
            if (failNextRefresh) {
                failNextRefresh = false
                throw IOException("fake network failure")
            }
        }

        override suspend fun clearAll() {
            state.value = emptyList()
        }
    }

    private fun cat(id: String, breedName: String): Cat = Cat(
        id = id,
        imageUrl = "https://example.test/$id.jpg",
        breed = Breed(
            id = "breed-$id",
            name = breedName,
            origin = "",
            temperament = "",
            lifeSpan = "",
            description = "",
        ),
    )

    private val bengal = cat(id = "b1", breedName = "Bengal")
    private val persian = cat(id = "p1", breedName = "Persian")

    @Test
    fun `blank query shows everything after the debounce window`() = runTest {
        val repository = FakeCatsRepository()
        repository.state.value = listOf(bengal, persian)
        val viewModel = CatsViewModel(repository)

        viewModel.cats.test {
            assertEquals(emptyList<Cat>(), awaitItem())

            advanceTimeBy(CatsViewModel.QUERY_DEBOUNCE_MILLIS.milliseconds)
            runCurrent()
            assertEquals(listOf(bengal, persian), awaitItem())
        }
    }

    @Test
    fun `query change narrows the results`() = runTest {
        val repository = FakeCatsRepository()
        repository.state.value = listOf(bengal, persian)
        val viewModel = CatsViewModel(repository)

        viewModel.cats.test {
            skipItems(1)
            advanceTimeBy(CatsViewModel.QUERY_DEBOUNCE_MILLIS.milliseconds)
            runCurrent()
            skipItems(1)

            viewModel.onQueryChanged("ben")
            advanceTimeBy(CatsViewModel.QUERY_DEBOUNCE_MILLIS.milliseconds)
            runCurrent()
            assertEquals(listOf(bengal), awaitItem())
        }
    }

    @Test
    fun `rapid typing subscribes only the final query`() = runTest {
        val repository = FakeCatsRepository()
        repository.state.value = listOf(bengal, persian)
        val viewModel = CatsViewModel(repository)

        viewModel.cats.test {
            skipItems(1)
            advanceTimeBy(CatsViewModel.QUERY_DEBOUNCE_MILLIS.milliseconds)
            runCurrent()
            skipItems(1)

            viewModel.onQueryChanged("b")
            advanceTimeBy(100.milliseconds)
            viewModel.onQueryChanged("be")
            advanceTimeBy(100.milliseconds)
            viewModel.onQueryChanged("ben")
            advanceTimeBy(CatsViewModel.QUERY_DEBOUNCE_MILLIS.milliseconds)
            runCurrent()

            assertEquals(listOf(bengal), awaitItem())
            assertEquals(listOf("", "ben"), repository.queriesSeen)
        }
    }

    @Test
    fun `onRefresh delegates to the repository`() = runTest {
        val repository = FakeCatsRepository()
        val viewModel = CatsViewModel(repository)

        viewModel.onRefresh()
        runCurrent()

        assertEquals(1, repository.refreshCount)
    }

    @Test
    fun `refresh failure emits one failure event`() = runTest {
        val repository = FakeCatsRepository().apply { failNextRefresh = true }
        val viewModel = CatsViewModel(repository)

        viewModel.refreshFailures.test {
            viewModel.onRefresh()
            runCurrent()

            awaitItem()
            expectNoEvents()
        }
    }

    @Test
    fun `refresh success emits no failure event`() = runTest {
        val repository = FakeCatsRepository()
        val viewModel = CatsViewModel(repository)

        viewModel.refreshFailures.test {
            viewModel.onRefresh()
            runCurrent()

            expectNoEvents()
        }
    }
}