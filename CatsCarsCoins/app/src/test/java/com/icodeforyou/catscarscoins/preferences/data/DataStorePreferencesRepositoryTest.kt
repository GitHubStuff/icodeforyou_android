// preferences/data/DataStorePreferencesRepositoryTest.kt
// CatsCarsCoins — spec 24.1.3. Complete file.
package com.icodeforyou.catscarscoins.preferences.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.turbine.test
import com.icodeforyou.catscarscoins.preferences.domain.AppPreferences
import com.icodeforyou.catscarscoins.preferences.domain.ThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class DataStorePreferencesRepositoryTest {

    @get:Rule
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    private fun TestScope.createRepository(): DataStorePreferencesRepository {
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            scope = backgroundScope.plus(UnconfinedTestDispatcher(testScheduler)),
            produceFile = { temporaryFolder.newFile("test.preferences_pb") },
        )
        return DataStorePreferencesRepository(dataStore)
    }

    @Test
    fun `cold start emits DEFAULTS`() = runTest {
        val repository = createRepository()

        repository.preferences.test {
            assertEquals(AppPreferences.DEFAULTS, awaitItem())
        }
    }

    @Test
    fun `setThemeMode persists and emits the new mode`() = runTest {
        val repository = createRepository()

        repository.setThemeMode(ThemeMode.LIGHT)

        repository.preferences.test {
            assertEquals(ThemeMode.LIGHT, awaitItem().themeMode)
        }
    }

    @Test
    fun `setPollingIntervalSeconds persists an in-range value`() = runTest {
        val repository = createRepository()

        repository.setPollingIntervalSeconds(17)

        repository.preferences.test {
            assertEquals(17, awaitItem().pollingIntervalSeconds)
        }
    }

    @Test
    fun `setPollingIntervalSeconds clamps below minimum to minimum`() = runTest {
        val repository = createRepository()

        repository.setPollingIntervalSeconds(0)

        repository.preferences.test {
            assertEquals(
                AppPreferences.POLLING_INTERVAL_MIN_SECONDS,
                awaitItem().pollingIntervalSeconds,
            )
        }
    }

    @Test
    fun `setPollingIntervalSeconds clamps above maximum to maximum`() = runTest {
        val repository = createRepository()

        repository.setPollingIntervalSeconds(99)

        repository.preferences.test {
            assertEquals(
                AppPreferences.POLLING_INTERVAL_MAX_SECONDS,
                awaitItem().pollingIntervalSeconds,
            )
        }
    }

    @Test
    fun `setPollingPaused false round-trips`() = runTest {
        val repository = createRepository()

        repository.setPollingPaused(false)

        repository.preferences.test {
            assertEquals(false, awaitItem().pollingPaused)
        }
    }

    @Test
    fun `setHapticsEnabled false round-trips`() = runTest {
        val repository = createRepository()

        repository.setHapticsEnabled(false)

        repository.preferences.test {
            assertEquals(false, awaitItem().hapticsEnabled)
        }
    }

    @Test
    fun `resetToDefaults restores DEFAULTS after every key changed`() = runTest {
        val repository = createRepository()
        repository.setThemeMode(ThemeMode.LIGHT)
        repository.setPollingIntervalSeconds(30)
        repository.setPollingPaused(false)
        repository.setHapticsEnabled(false)

        repository.resetToDefaults()

        repository.preferences.test {
            assertEquals(AppPreferences.DEFAULTS, awaitItem())
        }
    }
}