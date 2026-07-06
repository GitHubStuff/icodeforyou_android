// dbviewer/ui/DbViewerViewModel.kt
// CatsCarsCoins — spec 24.5.11. Complete file.
package com.icodeforyou.catscarscoins.dbviewer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icodeforyou.catscarscoins.coins.domain.CoinRefresher
import com.icodeforyou.catscarscoins.dbviewer.data.DbRow
import com.icodeforyou.catscarscoins.dbviewer.domain.DbViewerRepository
import com.icodeforyou.catscarscoins.dbviewer.domain.ResetAppUseCase
import com.icodeforyou.catscarscoins.ui.SUBSCRIPTION_STOP_TIMEOUT_MS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UI state representation for the schemaless table space explorer interface.
 */
data class DbViewerUiState(
    val tables: List<String> = emptyList(),
    val selectedTable: String = "",
    val headers: List<String> = emptyList(),
    val rows: List<DbRow> = emptyList()
)

/**
 * Presentation coordinator logic managing active database exploration view structures.
 */
class DbViewerViewModel(
    private val repository: DbViewerRepository,
    private val coinRefresher: CoinRefresher,
    private val resetAppUseCase: ResetAppUseCase
) : ViewModel() {

    private val selectedTableState = MutableStateFlow("")
    private val headersState = MutableStateFlow<List<String>>(emptyList())
    private val rowsState = MutableStateFlow<List<DbRow>>(emptyList())

    val uiState: StateFlow<DbViewerUiState> = combine(
        repository.tableNames(),
        selectedTableState,
        headersState,
        rowsState
    ) { tables, selected, headers, rows ->
        DbViewerUiState(
            tables = tables,
            selectedTable = selected,
            headers = headers,
            rows = rows
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(SUBSCRIPTION_STOP_TIMEOUT_MS),
        initialValue = DbViewerUiState()
    )

    /**
     * Updates target selection parameters and requests a tabular cell metadata fetch sweep.
     */
    fun selectTable(tableName: String) {
        viewModelScope.launch {
            selectedTableState.value = tableName
            if (tableName.isEmpty()) {
                headersState.value = emptyList()
                rowsState.value = emptyList()
            } else {
                val (headers, rows) = repository.queryTable(tableName)
                headersState.value = headers
                rowsState.value = rows
            }
        }
    }

    /**
     * Forces global engine update processing down onto background sync channels.
     */
    fun refreshData() {
        viewModelScope.launch {
            try {
                coinRefresher.refresh()
                val currentTable = selectedTableState.value
                if (currentTable.isNotEmpty()) {
                    val (headers, rows) = repository.queryTable(currentTable)
                    headersState.value = headers
                    rowsState.value = rows
                }
            } catch (e: Exception) {
                // Sunk structural exceptions handled silently
            }
        }
    }

    /**
     * Triggers dynamic system state destruction pipeline across database engines.
     */
    fun resetApplication() {
        viewModelScope.launch {
            try {
                resetAppUseCase.execute()
                selectedTableState.value = ""
                headersState.value = emptyList()
                rowsState.value = emptyList()
            } catch (e: Exception) {
                // Sunk structural exceptions handled silently
            }
        }
    }
}