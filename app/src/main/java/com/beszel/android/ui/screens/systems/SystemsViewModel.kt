package com.beszel.android.ui.screens.systems

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.beszel.android.data.model.System
import com.beszel.android.data.model.SystemStatus
import com.beszel.android.data.repository.SystemsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SystemFilter { All, Up, Warning, Down, Paused }

data class SystemsUiState(
    val systems: List<System> = emptyList(),
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val error: String? = null,
    val filter: SystemFilter = SystemFilter.All,
    val searchQuery: String = "",
    val searchActive: Boolean = false,
) {
    val filtered: List<System> get() {
        var list = systems
        if (filter != SystemFilter.All) {
            list = list.filter { sys ->
                when (filter) {
                    SystemFilter.Up      -> sys.status == SystemStatus.Up
                    SystemFilter.Warning -> sys.status == SystemStatus.Unknown // warn
                    SystemFilter.Down    -> sys.status == SystemStatus.Down
                    SystemFilter.Paused  -> sys.status == SystemStatus.Paused
                    SystemFilter.All     -> true
                }
            }
        }
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter { it.name.lowercase().contains(q) || it.host.lowercase().contains(q) }
        }
        return list
    }

    fun countFor(f: SystemFilter): Int = when (f) {
        SystemFilter.All     -> systems.size
        SystemFilter.Up      -> systems.count { it.status == SystemStatus.Up }
        SystemFilter.Warning -> systems.count { it.status == SystemStatus.Unknown }
        SystemFilter.Down    -> systems.count { it.status == SystemStatus.Down }
        SystemFilter.Paused  -> systems.count { it.status == SystemStatus.Paused }
    }
}

class SystemsViewModel(
    private val repo: SystemsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SystemsUiState(loading = true))
    val uiState = _uiState.asStateFlow()

    init { load() }

    fun load() {
        _uiState.update { it.copy(loading = it.systems.isEmpty(), error = null) }
        viewModelScope.launch {
            repo.getSystems()
                .onSuccess { systems ->
                    _uiState.update { it.copy(systems = systems, loading = false, refreshing = false, error = null) }
                }
                .onFailure { err ->
                    _uiState.update { it.copy(loading = false, refreshing = false, error = err.message) }
                }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(refreshing = true, error = null) }
        viewModelScope.launch {
            repo.getSystems()
                .onSuccess { systems ->
                    _uiState.update { it.copy(systems = systems, refreshing = false, error = null) }
                }
                .onFailure { err ->
                    _uiState.update { it.copy(refreshing = false, error = err.message) }
                }
        }
    }

    fun setFilter(f: SystemFilter) = _uiState.update { it.copy(filter = f) }
    fun setSearchQuery(q: String) = _uiState.update { it.copy(searchQuery = q) }
    fun setSearchActive(active: Boolean) = _uiState.update {
        it.copy(searchActive = active, searchQuery = if (!active) "" else it.searchQuery)
    }

    class Factory(private val repo: SystemsRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SystemsViewModel(repo) as T
        }
    }
}
