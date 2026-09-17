package com.bakeerp.app.ui.sale

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.bakeerp.app.BakeErpApplication
import com.bakeerp.app.data.database.entity.CustomerEntity
import com.bakeerp.app.data.database.entity.SaleRecordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 销售模块 ViewModel。
 */
class SaleViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val app = application as BakeErpApplication

    val sales: StateFlow<List<SaleRecordEntity>> = app.saleRepository
        .observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val customers: StateFlow<List<CustomerEntity>> = app.customerRepository
        .observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _form = MutableStateFlow(SaleFormState())
    val form: StateFlow<SaleFormState> = _form.asStateFlow()

    fun updateForm(transform: (SaleFormState) -> SaleFormState) {
        _form.value = transform(_form.value)
    }

    fun submit(onDone: (Long) -> Unit = {}) {
        val f = _form.value
        if (f.productName.isBlank() || f.quantity <= 0.0 || f.unitPrice < 0.0) return
        viewModelScope.launch(Dispatchers.IO) {
            // 自动维护客户档案
            val name = f.customerName.trim()
            if (name.isNotEmpty()) {
                val existing = app.customerRepository.findByName(name)
                val updated = existing?.copy(
                    totalOrders = existing.totalOrders + 1,
                    totalSpent = existing.totalSpent + f.quantity * f.unitPrice
                ) ?: CustomerEntity(
                    name = name,
                    totalOrders = 1,
                    totalSpent = f.quantity * f.unitPrice
                )
                app.customerRepository.upsert(updated)
            }
            val record = SaleRecordEntity(
                productName = f.productName.trim(),
                quantity = f.quantity,
                unitPrice = f.unitPrice,
                totalPrice = f.quantity * f.unitPrice,
                customerName = name,
                paymentMethod = f.paymentMethod,
                saleDate = System.currentTimeMillis(),
                notes = f.notes
            )
            val id = app.saleRepository.saveSale(record)
            _form.value = SaleFormState()
            onDone(id)
        }
    }
}

/**
 * 销售登记表单状态。
 */
data class SaleFormState(
    val productName: String = "",
    val quantity: Double = 1.0,
    val unitPrice: Double = 0.0,
    val customerName: String = "",
    val paymentMethod: String = "现金",
    val notes: String = ""
)

class SaleViewModelFactory(private val app: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val handle = androidx.lifecycle.SavedStateHandleSupport.createSavedStateHandle(extras)
        return SaleViewModel(app, handle) as T
    }
}