// cats/ui/CatsViewModel.kt
// CatsCarsCoins — spec 24.3.19. Complete file.
package com.icodeforyou.catscarscoins.cats.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icodeforyou.catscarscoins.cats.domain.Cat
import com.icodeforyou.catscarscoins.cats.domain.CatsRepository
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
 * Cats search state (Phase 3 UI). The query pipeline — debounce then
 * flatMapLatest into the repository's single observable — is the
 * ViewModel's whole job (pinned by 24.3.18: rapid typing subscribes only
 * the final query; keystrokes never hammer FTS). Refresh failures are
 * signaled, never presented: the screen owns the 750 ms read-only toast
 * (24.3.2 contract).
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class CatsViewModel(
    private val catsRepository: CatsRepository,
) : ViewModel() {

    private val mutableQuery = MutableStateFlow("")
    val query: StateFlow<String> = mutableQuery.asStateFlow()

    val cats: StateFlow<List<Cat>> = mutableQuery
        .debounce(QUERY_DEBOUNCE_MILLIS)
        .flatMapLatest { query -> catsRepository.cats(query) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_STOP_TIMEOUT_MS),
            initialValue = emptyList(),
        )

    private val mutableRefreshFailures = MutableSharedFlow<Unit>()
    val refreshFailures: SharedFlow<Unit> = mutableRefreshFailures.asSharedFlow()

    fun onQueryChanged(value: String) {
        mutableQuery.value = value
    }

    fun onRefresh() {
        viewModelScope.launch {
            try {
                catsRepository.refresh()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                mutableRefreshFailures.emit(Unit)
            }
        }
    }

    companion object {

        /** Keystroke settle window before the query reaches FTS. */
        const val QUERY_DEBOUNCE_MILLIS: Long = 300L
    }
}