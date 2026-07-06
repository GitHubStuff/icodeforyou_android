// cars/ui/CarsViewModel.kt
// CatsCarsCoins — spec 24.4.16. Complete file.
package com.icodeforyou.catscarscoins.cars.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icodeforyou.catscarscoins.cars.domain.CarsRepository
import com.icodeforyou.catscarscoins.cars.domain.Manufacturer
import com.icodeforyou.catscarscoins.ui.SUBSCRIPTION_STOP_TIMEOUT_MS
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Cars search state (Phase 4 UI). The query pipeline — debounce then
 * flatMapLatest into the repository's single observable — is the
 * ViewModel's whole job (parallel to CatsViewModel, pinned by 24.4.15:
 * rapid typing subscribes only the final query; keystrokes never hammer
 * FTS). Refresh failures are signaled, never presented: the screen owns
 * the 750 ms read-only toast.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class CarsViewModel(
    private val carsRepository: CarsRepository,
) : ViewModel() {

    private val mutableQuery = MutableStateFlow("")
    val query: StateFlow<String> = mutableQuery.asStateFlow()

    val manufacturers: StateFlow<List<Manufacturer>> = mutableQuery
        .debounce(QUERY_DEBOUNCE_MILLIS)
        .flatMapLatest { query -> carsRepository.manufacturers(query) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_STOP_TIMEOUT_MS),
            initialValue = emptyList(),
        )

    private val mutableRefreshFailures = MutableSharedFlow<Unit>()
    val refreshFailures: SharedFlow<Unit> = mutableRefreshFailures.asSharedFlow()

    private val mutableIsRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = mutableIsRefreshing.asStateFlow()

    fun onQueryChanged(value: String) {
        mutableQuery.value = value
    }

    fun onRefresh() {
        viewModelScope.launch {
            mutableIsRefreshing.value = true
            try {
                carsRepository.refresh()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                mutableRefreshFailures.emit(Unit)
            } finally {
                mutableIsRefreshing.value = false
            }
        }
    }

    companion object {

        /** Keystroke settle window before the query reaches FTS. */
        const val QUERY_DEBOUNCE_MILLIS: Long = 300L
    }
}