package com.bakeerp.app.ui.material

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.bakeerp.app.BakeErpApplication
import com.bakeerp.app.data.database.entity.MaterialEntity
import com.bakeerp.app.data.database.entity.PurchaseRecordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 原材料模块 ViewModel：列表 + 详情 + 新增/编辑 + 新增采购。
 */
class MaterialViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val app = application as BakeErpApplication

    val materials: StateFlow<List<MaterialEntity>> = app.materialRepository
        .observeAllMaterials()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _editing = MutableStateFlow<MaterialEntity?>(null)
    val editing: StateFlow<MaterialEntity?> = _editing.asStateFlow()

    fun loadMaterial(id: Long) {
        if (id <= 0L) {
            _editing.value = null
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _editing.value = app.database.materialDao().findById(id)
        }
    }

    fun observeMaterial(id: Long) = app.materialRepository.observeMaterial(id)

    fun observeHistory(id: Long) = app.materialRepository.observePurchaseHistory(id)

    fun saveMaterial(entity: MaterialEntity, onDone: (Long) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = app.materialRepository.upsertMaterial(entity.copy(updatedAt = System.currentTimeMillis()))
            onDone(id)
        }
    }

    fun deleteMaterial(entity: MaterialEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            app.materialRepository.deleteMaterial(entity)
        }
    }

    fun addPurchase(record: PurchaseRecordEntity, onDone: (Long) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = app.materialRepository.addPurchase(record)
            onDone(id)
        }
    }

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            val handle = androidx.lifecycle.SavedStateHandleSupport.createSavedStateHandle(extras)
            return MaterialViewModel(app, handle) as T
        }
    }
}