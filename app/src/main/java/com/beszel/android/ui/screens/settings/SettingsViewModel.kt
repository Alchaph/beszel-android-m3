package com.beszel.android.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.beszel.android.data.repository.AppSettings
import com.beszel.android.data.repository.SettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repo: SettingsRepository,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = repo.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    fun setDarkMode(v: Boolean) { viewModelScope.launch { repo.setDarkMode(v) } }
    fun setAccentColor(v: String) { viewModelScope.launch { repo.setAccentColor(v) } }
    fun setDensity(v: String) { viewModelScope.launch { repo.setDensity(v) } }

    class Factory(private val repo: SettingsRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repo) as T
        }
    }
}
