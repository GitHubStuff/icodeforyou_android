// preferences/ui/ThemeViewModelTest.kt
// CatsCarsCoins — spec 24.1.7. Complete file.
package com.icodeforyou.catscarscoins.preferences.ui

import app.cash.turbine.test
import com.icodeforyou.catscarscoins.preferences.domain.AppPreferences
import com.icodeforyou.catscarscoins.preferences.domain.PreferencesRepository
import com.icodeforyou.catscarscoins.preferences.domain.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeViewModelTest {

    private class FakePreferencesRepository : PreferencesRepository {

        private val state = MutableStateFlow(AppPreferences.DEFAULTS)

        override val preferences: Flow<AppPreferences> = state

        override suspend fun setThemeMode(themeMode: ThemeMode) {
            state.update { it.copy(themeMode = themeMode) }
        }

        override suspend fun setPollingIntervalSeconds(seconds: Int) {
            state.update { it.copy(pollingIntervalSeconds = seconds) }
        }

        override suspend fun setPollingPaused(paused: Boolean) {
            state.update { it.copy(pollingPaused = paused) }
        }

        override suspend fun setHapticsEnabled(enabled: Boolean) {
            state.update { it.copy(hapticsEnabled = enabled) }
        }

        override suspend fun resetToDefaults() {
            state.value = AppPreferences.DEFAULTS
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial theme is the spec default`() = runTest {
        val viewModel = ThemeViewModel(FakePreferencesRepository())

        assertEquals(AppPreferences.DEFAULTS.themeMode, viewModel.themeMode.value)
    }

    @Test
    fun `theme changes in the repository reach themeMode`() = runTest {
        val repository = FakePreferencesRepository()
        val viewModel = ThemeViewModel(repository)

        viewModel.themeMode.test {
            assertEquals(ThemeMode.DARK, awaitItem())

            repository.setThemeMode(ThemeMode.LIGHT)
            assertEquals(ThemeMode.LIGHT, awaitItem())

            repository.setThemeMode(ThemeMode.SYSTEM)
            assertEquals(ThemeMode.SYSTEM, awaitItem())
        }
    }

    @Test
    fun `non-theme preference changes emit nothing`() = runTest {
        val repository = FakePreferencesRepository()
        val viewModel = ThemeViewModel(repository)

        viewModel.themeMode.test {
            assertEquals(ThemeMode.DARK, awaitItem())

            repository.setHapticsEnabled(false)
            repository.setPollingPaused(false)
            repository.setPollingIntervalSeconds(30)
            expectNoEvents()
        }
    }
}
