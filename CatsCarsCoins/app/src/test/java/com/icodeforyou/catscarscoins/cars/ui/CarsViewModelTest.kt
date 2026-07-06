// cars/ui/CarsViewModelTest.kt
// CatsCarsCoins — spec 24.4.15. Complete file. Test sources.
package com.icodeforyou.catscarscoins.cars.ui

import app.cash.turbine.test
import com.icodeforyou.catscarscoins.cars.domain.FakeCarsRepository
import com.icodeforyou.catscarscoins.cars.domain.Manufacturer
import com.icodeforyou.catscarscoins.testsupport.MainDispatcherRule
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CarsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val tesla = Manufacturer(
        id = 955,
        name = "TESLA, INC.",
        country = "UNITED STATES (USA)",
    )

    private val bmw = Manufacturer(
        id = 967,
        name = "BMW AG",
        country = "GERMANY",
    )

    @Test
    fun `blank query shows everything after the debounce window`() = runTest {
        val repository = FakeCarsRepository(initial = listOf(tesla, bmw))
        val viewModel = CarsViewModel(repository)

        viewModel.manufacturers.test {
            assertEquals(emptyList<Manufacturer>(), awaitItem())

            advanceTimeBy(CarsViewModel.QUERY_DEBOUNCE_MILLIS.milliseconds)
            runCurrent()
            assertEquals(listOf(tesla, bmw), awaitItem())
        }
    }

    @Test
    fun `query change narrows the results`() = runTest {
        val repository = FakeCarsRepository(initial = listOf(tesla, bmw))
        val viewModel = CarsViewModel(repository)

        viewModel.manufacturers.test {
            skipItems(1)
            advanceTimeBy(CarsViewModel.QUERY_DEBOUNCE_MILLIS.milliseconds)
            runCurrent()
            skipItems(1)

            viewModel.onQueryChanged("tes")
            advanceTimeBy(CarsViewModel.QUERY_DEBOUNCE_MILLIS.milliseconds)
            runCurrent()
            assertEquals(listOf(tesla), awaitItem())
        }
    }

    @Test
    fun `query matches the country field`() = runTest {
        val repository = FakeCarsRepository(initial = listOf(tesla, bmw))
        val viewModel = CarsViewModel(repository)

        viewModel.manufacturers.test {
            skipItems(1)
            advanceTimeBy(CarsViewModel.QUERY_DEBOUNCE_MILLIS.milliseconds)
            runCurrent()
            skipItems(1)

            viewModel.onQueryChanged("germany")
            advanceTimeBy(CarsViewModel.QUERY_DEBOUNCE_MILLIS.milliseconds)
            runCurrent()
            assertEquals(listOf(bmw), awaitItem())
        }
    }

    @Test
    fun `rapid typing subscribes only the final query`() = runTest {
        val repository = FakeCarsRepository(initial = listOf(tesla, bmw))
        val viewModel = CarsViewModel(repository)

        viewModel.manufacturers.test {
            skipItems(1)
            advanceTimeBy(CarsViewModel.QUERY_DEBOUNCE_MILLIS.milliseconds)
            runCurrent()
            skipItems(1)

            viewModel.onQueryChanged("t")
            advanceTimeBy(100.milliseconds)
            viewModel.onQueryChanged("te")
            advanceTimeBy(100.milliseconds)
            viewModel.onQueryChanged("tes")
            advanceTimeBy(CarsViewModel.QUERY_DEBOUNCE_MILLIS.milliseconds)
            runCurrent()

            assertEquals(listOf(tesla), awaitItem())
            assertEquals(listOf("", "tes"), repository.queriesSeen)
        }
    }

    @Test
    fun `onRefresh delegates to the repository`() = runTest {
        val repository = FakeCarsRepository()
        val viewModel = CarsViewModel(repository)

        viewModel.onRefresh()
        runCurrent()

        assertEquals(1, repository.refreshCount)
    }

    @Test
    fun `refresh failure emits one failure event`() = runTest {
        val repository = FakeCarsRepository().apply { failNextRefresh = true }
        val viewModel = CarsViewModel(repository)

        viewModel.refreshFailures.test {
            viewModel.onRefresh()
            runCurrent()

            awaitItem()
            expectNoEvents()
        }
    }

    @Test
    fun `refresh success emits no failure event`() = runTest {
        val repository = FakeCarsRepository()
        val viewModel = CarsViewModel(repository)

        viewModel.refreshFailures.test {
            viewModel.onRefresh()
            runCurrent()

            expectNoEvents()
        }
    }

    @Test
    fun `isRefreshing starts false`() = runTest {
        val viewModel = CarsViewModel(FakeCarsRepository())

        assertFalse(viewModel.isRefreshing.value)
    }

    @Test
    fun `isRefreshing is true in-flight and false after success`() = runTest {
        val repository = FakeCarsRepository()
        val gate = CompletableDeferred<Unit>()
        repository.refreshGate = gate
        val viewModel = CarsViewModel(repository)

        viewModel.isRefreshing.test {
            assertFalse(awaitItem())

            viewModel.onRefresh()
            runCurrent()
            assertTrue(awaitItem())

            gate.complete(Unit)
            runCurrent()
            assertFalse(awaitItem())
        }
    }

    @Test
    fun `isRefreshing clears after a failed refresh`() = runTest {
        val repository = FakeCarsRepository().apply { failNextRefresh = true }
        val gate = CompletableDeferred<Unit>()
        repository.refreshGate = gate
        val viewModel = CarsViewModel(repository)

        viewModel.isRefreshing.test {
            assertFalse(awaitItem())

            viewModel.onRefresh()
            runCurrent()
            assertTrue(awaitItem())

            gate.complete(Unit)
            runCurrent()
            assertFalse(awaitItem())
        }
    }
}