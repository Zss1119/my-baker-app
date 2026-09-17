package com.bakeerp.app.ui.recipe

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.bakeerp.app.BakeErpApplication
import com.bakeerp.app.data.database.entity.MaterialEntity
import com.bakeerp.app.data.database.entity.RecipeEntity
import com.bakeerp.app.data.database.entity.RecipeItemEntity
import com.bakeerp.app.data.repository.RecipeDetail
import com.bakeerp.app.data.repository.RecipeItemWithMaterial
import com.bakeerp.app.data.repository.RecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 配方模块 ViewModel。
 */
class RecipeViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val app = application as BakeErpApplication

    val recipes: StateFlow<List<RecipeEntity>> = app.recipeRepository
        .observeAllRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val materials: StateFlow<List<MaterialEntity>> = app.materialRepository
        .observeAllMaterials()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _editing = MutableStateFlow<RecipeEntity?>(null)
    val editing: StateFlow<RecipeEntity?> = _editing.asStateFlow()

    private val _items = MutableStateFlow<List<RecipeItemEntity>>(emptyList())
    val items: StateFlow<List<RecipeItemEntity>> = _items.asStateFlow()

    fun loadRecipe(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            if (id <= 0L) {
                _editing.value = null
                _items.value = emptyList()
                return@launch
            }
            _editing.value = app.recipeRepository.findRecipe(id)
            _items.value = app.database.recipeItemDao().listByRecipe(id)
        }
    }

    fun observeDetail(id: Long): StateFlow<RecipeDetail> = app.recipeRepository
        .observeRecipeDetail(id, materials)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecipeDetail(null, emptyList()))

    fun setRecipe(entity: RecipeEntity?) {
        _editing.value = entity
    }

    fun setItems(items: List<RecipeItemEntity>) {
        _items.value = items
    }

    fun addItem(materialId: Long, quantity: Double) {
        _items.value = _items.value + RecipeItemEntity(recipeId = 0L, materialId = materialId, quantityPerUnit = quantity)
    }

    fun removeItem(index: Int) {
        val list = _items.value.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _items.value = list
        }
    }

    fun saveRecipe(onDone: (Long) -> Unit = {}) {
        val recipe = _editing.value ?: return
        val current = _items.value
        viewModelScope.launch(Dispatchers.IO) {
            val id = app.recipeRepository.saveRecipe(recipe, current)
            onDone(id)
        }
    }

    fun deleteRecipe(recipe: RecipeEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            app.recipeRepository.deleteRecipe(recipe)
        }
    }

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            val handle = extras.createSavedStateHandle()
            return RecipeViewModel(app, handle) as T
        }
    }
}