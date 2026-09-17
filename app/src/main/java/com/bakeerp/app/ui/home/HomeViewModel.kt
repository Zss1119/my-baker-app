package com.bakeerp.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bakeerp.app.BakeErpApplication
import com.bakeerp.app.data.database.entity.MaterialEntity
import com.bakeerp.app.data.database.entity.PurchaseRecordEntity
import com.bakeerp.app.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 首页数据：库存预警 + 保质期预警 + 今日销售 + 本月利润。
 */
data class HomeUiState(
    val lowStockCount: Int = 0,
    val expiringCount: Int = 0,
    val todayRevenue: Double = 0.0,
    val monthRevenue: Double = 0.0,
    val monthCost: Double = 0.0
) {
    val monthProfit: Double get() = monthRevenue - monthCost
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as BakeErpApplication

    val lowStockMaterials: StateFlow<List<MaterialEntity>> = app.materialRepository
        .observeLowStock()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val expiringBatches: StateFlow<List<PurchaseRecordEntity>> = app.materialRepository
        .observeAllByExpiry()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                app.materialRepository.observeLowStock(),
                app.materialRepository.observeAllByExpiry()
            ) { low, expiring ->
                Pair(low.size, expiring.count { batch ->
                    val ts = batch.expiryDate ?: return@count false
                    ts <= System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000
                            && batch.remainingQuantity > 0.0
                })
            }.collect { (low, exp) ->
                val todayStart = DateUtils.startOfToday()
                val todayEnd = DateUtils.endOfToday()
                val monthStart = DateUtils.startOfMonth()
                val monthEnd = DateUtils.endOfMonth()
                val today = app.saleRepository.sumRevenue(todayStart, todayEnd)
                val monthRevenue = app.saleRepository.sumRevenue(monthStart, monthEnd)
                val productions = app.productionRepository.listInRange(monthStart, monthEnd)
                val monthCost = productions.sumOf { it.totalMaterialCost }
                _uiState.value = HomeUiState(
                    lowStockCount = low,
                    expiringCount = exp,
                    todayRevenue = today,
                    monthRevenue = monthRevenue,
                    monthCost = monthCost
                )
            }
        }
    }

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(app) as T
        }
    }
}