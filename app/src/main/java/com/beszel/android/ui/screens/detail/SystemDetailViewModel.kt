package com.beszel.android.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.beszel.android.data.model.Container
import com.beszel.android.data.model.System
import com.beszel.android.data.model.SystemDetails
import com.beszel.android.data.model.SystemStats
import com.beszel.android.data.repository.SystemsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

enum class DetailTab { System, Docker, Info }
enum class TimeRange(val type: String, val perPage: Int, val label: String) {
    OneHour("1m", 60, "1h"),
    TwelveHours("10m", 72, "12h"),
    TwentyFourHours("10m", 144, "24h"),
    OneWeek("120m", 84, "1w"),
    ThirtyDays("480m", 90, "30d"),
}

data class SystemDetailUiState(
    val system: System? = null,
    val details: SystemDetails? = null,
    val stats: List<SystemStats> = emptyList(),
    val containers: List<Container> = emptyList(),
    val loading: Boolean = true,
    val statsLoading: Boolean = false,
    val error: String? = null,
    val statsError: String? = null,
    val activeTab: DetailTab = DetailTab.System,
    val timeRange: TimeRange = TimeRange.OneHour,
)

class SystemDetailViewModel(
    private val systemId: String,
    private val repo: SystemsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SystemDetailUiState())
    val uiState = _uiState.asStateFlow()

    init { loadAll() }

    private fun loadAll() {
        viewModelScope.launch {
            supervisorScope {
                launch { loadDetails() }
                launch { loadStats(_uiState.value.timeRange) }
                launch { loadContainers() }
            }
        }
    }

    private suspend fun loadDetails() {
        // The system record comes from the parent nav, but we still fetch details
        repo.getSystemDetails(systemId)
            .onSuccess { details ->
                _uiState.update { it.copy(details = details, loading = false) }
            }
            .onFailure { err ->
                _uiState.update { it.copy(loading = false, error = err.message) }
            }
    }

    private suspend fun loadStats(range: TimeRange) {
        _uiState.update { it.copy(statsLoading = true, statsError = null) }
        repo.getSystemStats(systemId, range.type, range.perPage)
            .onSuccess { stats ->
                _uiState.update { it.copy(stats = stats, statsLoading = false) }
            }
            .onFailure { err ->
                _uiState.update { it.copy(statsLoading = false, statsError = err.message) }
            }
    }

    private suspend fun loadContainers() {
        repo.getContainers(systemId)
            .onSuccess { containers ->
                _uiState.update { it.copy(containers = containers) }
            }
    }

    fun setTab(tab: DetailTab) = _uiState.update { it.copy(activeTab = tab) }

    fun setTimeRange(range: TimeRange) {
        _uiState.update { it.copy(timeRange = range) }
        viewModelScope.launch { loadStats(range) }
    }

    fun setSystem(system: System) = _uiState.update { it.copy(system = system) }

    fun refresh() {
        viewModelScope.launch { loadAll() }
    }

    class Factory(
        private val systemId: String,
        private val repo: SystemsRepository,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SystemDetailViewModel(systemId, repo) as T
        }
    }
}
