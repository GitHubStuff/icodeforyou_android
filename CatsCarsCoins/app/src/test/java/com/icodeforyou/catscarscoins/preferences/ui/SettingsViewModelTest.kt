// preferences/ui/SettingsViewModelTest.kt
// CatsCarsCoins — spec 24.5.14. Complete file. Test sources.
// Change from 24.1.20: constructor gains FakeResetAppUseCase (via the
// viewModel() builder so scenarios stay one-liners); onNukeDatabase
// delegation test added.
package com.icodeforyou.catscarscoins.preferences.ui

import app.cash.turbine.test
import com.icodeforyou.catscarscoins.dbviewer.domain.FakeResetAppUseCase
import com.icodeforyou.catscarscoins.preferences.domain.AppPreferences
import com.icodeforyou.catscarscoins.preferences.domain.FakePreferencesRepository
import com.icodeforyou.catscarscoins.preferences.domain.ThemeMode
import com.icodeforyou.catscarscoins.testsupport.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(
        repository: FakePreferencesRepository = FakePreferencesRepository(),
        resetAppUseCase: FakeResetAppUseCase = FakeResetAppUseCase(),
    ): SettingsViewModel = SettingsViewModel(
        preferencesRepository = repository,
        resetAppUseCase = resetAppUseCase,
    )

    @Test
    fun `initial state is the spec default`() = runTest {
        val viewModel = viewModel()

        assertEquals(AppPreferences.DEFAULTS, viewModel.preferences.value)
    }

    @Test
    fun `onThemeModeSelected persists and emits`() = runTest {
        val viewModel = viewModel()

        viewModel.preferences.test {
            assertEquals(AppPreferences.DEFAULTS, awaitItem())

            viewModel.onThemeModeSelected(ThemeMode.LIGHT)
            assertEquals(ThemeMode.LIGHT, awaitItem().themeMode)
        }
    }

    @Test
    fun `onPollingIntervalChanged persists an in-range value`() = runTest {
        val viewModel = viewModel()

        viewModel.preferences.test {
            assertEquals(AppPreferences.DEFAULTS, awaitItem())

            viewModel.onPollingIntervalChanged(17)
            assertEquals(17, awaitItem().pollingIntervalSeconds)
        }
    }

    @Test
    fun `onPollingIntervalChanged above maximum arrives clamped`() = runTest {
        val viewModel = viewModel()

        viewModel.preferences.test {
            assertEquals(AppPreferences.DEFAULTS, awaitItem())

            viewModel.onPollingIntervalChanged(99)
            assertEquals(
                AppPreferences.POLLING_INTERVAL_MAX_SECONDS,
                awaitItem().pollingIntervalSeconds,
            )
        }
    }

    @Test
    fun `onPollingPausedChanged persists and emits`() = runTest {
        val viewModel = viewModel()

        viewModel.preferences.test {
            assertEquals(AppPreferences.DEFAULTS, awaitItem())

            viewModel.onPollingPausedChanged(false)
            assertEquals(false, awaitItem().pollingPaused)
        }
    }

    @Test
    fun `onHapticsEnabledChanged persists and emits`() = runTest {
        val viewModel = viewModel()

        viewModel.preferences.test {
            assertEquals(AppPreferences.DEFAULTS, awaitItem())

            viewModel.onHapticsEnabledChanged(false)
            assertEquals(false, awaitItem().hapticsEnabled)
        }
    }

    @Test
    fun `onNukeDatabase delegates to the use case`() = runTest {
        val resetAppUseCase = FakeResetAppUseCase()
        val viewModel = viewModel(resetAppUseCase = resetAppUseCase)

        viewModel.onNukeDatabase()
        runCurrent()

        assertEquals(1, resetAppUseCase.executeCount)
    }
}