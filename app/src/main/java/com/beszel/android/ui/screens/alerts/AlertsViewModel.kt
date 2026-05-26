package com.beszel.android.ui.screens.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.beszel.android.data.model.AlertEvent
import com.beszel.android.data.repository.AlertsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AlertsUiState(
    val events: List<AlertEvent> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
    val showResolved: Boolean = false,
) {
    val filtered: List<AlertEvent> get() =
        if (showResolved) events.filter { !it.isActive }
        else events.filter { it.isActive }

    val activeCount: Int get() = events.count { it.isActive }
    val resolvedCount: Int get() = events.count { !it.isActive }
}

class AlertsViewModel(
    private val userId: String,
    private val repo: AlertsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlertsUiState())
    val uiState = _uiState.asStateFlow()

    init { load() }

    fun load() {
        _uiState.update { it.copy(loading = it.events.isEmpty(), error = null) }
        viewModelScope.launch {
            repo.getAlertHistory(userId)
                .onSuccess { events ->
                    _uiState.update { it.copy(events = events, loading = false, error = null) }
                }
                .onFailure { err ->
                    _uiState.update { it.copy(loading = false, error = err.message) }
                }
        }
    }

    fun setShowResolved(v: Boolean) = _uiState.update { it.copy(showResolved = v) }

    class Factory(
        private val userId: String,
        private val repo: AlertsRepository,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AlertsViewModel(userId, repo) as T
        }
    }
}
