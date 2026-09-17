package com.bakeerp.app.ui.production

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.bakeerp.app.BakeErpApplication
import com.bakeerp.app.data.database.entity.MaterialEntity
import com.bakeerp.app.data.database.entity.ProductionRecordEntity
import com.bakeerp.app.data.database.entity.RecipeEntity
import com.bakeerp.app.data.repository.ConsumedItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 制作模块 ViewModel：选配方 → 预览原料消耗 → 确认扣减。
 */
class ProductionViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val app = application as BakeErpApplication

    val recipes: StateFlow<List<RecipeEntity>> = app.recipeRepository
        .observeAllRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val productions: StateFlow<List<ProductionRecordEntity>> = app.productionRepository
        .observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val materials: StateFlow<List<MaterialEntity>> = app.materialRepository
        .observeAllMaterials()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedRecipeId = MutableStateFlow(0L)
    val selectedRecipeId: StateFlow<Long> = _selectedRecipeId.asStateFlow()

    private val _batchCount = MutableStateFlow(1.0)
    val batchCount: StateFlow<Double> = _batchCount.asStateFlow()

    private val _producedQuantity = MutableStateFlow(0.0)
    val producedQuantity: StateFlow<Double> = _producedQuantity.asStateFlow()

    private val _previewItems = MutableStateFlow<List<ConsumedItem>>(emptyList())
    val previewItems: StateFlow<List<ConsumedItem>> = _previewItems.asStateFlow()

    private val _totalCost = MutableStateFlow(0.0)
    val totalCost: StateFlow<Double> = _totalCost.asStateFlow()

    fun setRecipe(id: Long) {
        _selectedRecipeId.value = id
        recomputePreview()
    }

    fun setBatch(value: Double) {
        _batchCount.value = value
        recomputePreview()
    }

    fun setProduced(value: Double) {
        _producedQuantity.value = value
    }

    private fun recomputePreview() {
        val recipeId = _selectedRecipeId.value
        val batch = _batchCount.value
        if (recipeId <= 0L || batch <= 0.0) {
            _previewItems.value = emptyList()
            _totalCost.value = 0.0
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val recipe = app.recipeRepository.findRecipe(recipeId) ?: return@launch
            val items = app.database.recipeItemDao().listByRecipe(recipeId)
            val materialMap = materials.value.associateBy { it.id }
            val preview = items.map { item ->
                val qty = item.quantityPerUnit * batch
                val m = materialMap[item.materialId]
                val unitPrice = m?.pricePerUnit ?: 0.0
                ConsumedItem(
                    materialId = item.materialId,
                    materialName = m?.name ?: "(未知原料)",
                    unit = m?.unit ?: "",
                    quantity = qty,
                    unitPrice = unitPrice,
                    subtotalCost = qty * unitPrice
                )
            }
            val total = preview.sumOf { it.subtotalCost }
            _previewItems.value = preview
            _totalCost.value = total
            // 默认产出 = 配方期望产出 × 批次量
            if (_producedQuantity.value == 0.0) {
                _producedQuantity.value = recipe.expectedOutput * batch
            }
        }
    }

    fun confirmProduction(onDone: (Long) -> Unit = {}) {
        val recipeId = _selectedRecipeId.value
        if (recipeId <= 0L) return
        viewModelScope.launch(Dispatchers.IO) {
            val items = _previewItems.value
            // 1. 扣减库存
            items.forEach { item ->
                app.materialRepository.consumeMaterial(item.materialId, item.quantity)
            }
            // 2. 落库制作记录
            val record = ProductionRecordEntity(
                recipeId = recipeId,
                batchCount = _batchCount.value,
                producedQuantity = _producedQuantity.value,
                productionDate = System.currentTimeMillis(),
                totalMaterialCost = _totalCost.value
            )
            val id = app.productionRepository.saveProduction(record)
            // 3. 重置表单
            _selectedRecipeId.value = 0L
            _batchCount.value = 1.0
            _producedQuantity.value = 0.0
            _previewItems.value = emptyList()
            _totalCost.value = 0.0
            onDone(id)
        }
    }

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            val handle = androidx.lifecycle.SavedStateHandleSupport.createSavedStateHandle(extras)
            return ProductionViewModel(app, handle) as T
        }
    }
}