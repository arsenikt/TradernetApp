package com.tradernet.quotes.ui.settings

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tradernet.quotes.data.SettingsRepository
import com.tradernet.quotes.domain.AppSettings
import com.tradernet.quotes.domain.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = repository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppSettings.Default,
        )

    fun setExtendedMode(enabled: Boolean) {
        viewModelScope.launch { repository.setExtendedMode(enabled) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                SettingsViewModel(SettingsRepository(app.applicationContext))
            }
        }
    }
}
