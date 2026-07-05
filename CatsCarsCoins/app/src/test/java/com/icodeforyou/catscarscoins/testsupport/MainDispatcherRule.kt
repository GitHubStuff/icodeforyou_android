// testsupport/MainDispatcherRule.kt
// CatsCarsCoins — spec 24.1.18. Complete file. Test sources.
package com.icodeforyou.catscarscoins.testsupport

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Swaps [Dispatchers.Main] for a [TestDispatcher] around each test —
 * required by anything touching viewModelScope. One rule, declared once
 * per test class:
 *
 * ```
 * @get:Rule
 * val mainDispatcherRule = MainDispatcherRule()
 * ```
 *
 * Defaults to [UnconfinedTestDispatcher] (eager execution — right for
 * StateFlow pipelines); pass a StandardTestDispatcher when a test needs
 * explicit scheduling control.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}