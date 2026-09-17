package com.bakeerp.app.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.bakeerp.app.BakeErpApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 设置模块 ViewModel：提醒开关、数据备份/恢复/清空等。
 *
 * 当前实现：仅暴露提醒开关的 UI 状态。备份/恢复/清空等动作在 UI 层直接调用 Repository，避免 ViewModel 膨胀。
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as BakeErpApplication

    private val _reminderEnabled = MutableStateFlow(true)
    val reminderEnabled: StateFlow<Boolean> = _reminderEnabled.asStateFlow()

    fun toggleReminder(enabled: Boolean) {
        _reminderEnabled.value = enabled
    }

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return SettingsViewModel(app) as T
        }
    }
}