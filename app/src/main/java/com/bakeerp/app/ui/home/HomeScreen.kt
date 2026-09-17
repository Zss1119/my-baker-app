package com.bakeerp.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bakeerp.app.R
import com.bakeerp.app.ui.components.SummaryCard
import com.bakeerp.app.util.DateUtils

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as com.bakeerp.app.BakeErpApplication
    val vm: HomeViewModel = viewModel(factory = HomeViewModel.Factory(app))

    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val lowStock by vm.lowStockMaterials.collectAsStateWithLifecycle()
    val expiring by vm.expiringBatches.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text(
                    text = stringResource(R.string.home_greeting),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = DateUtils.formatDate(System.currentTimeMillis()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(
                    title = stringResource(R.string.home_card_inventory_warning),
                    value = "${uiState.lowStockCount} 项",
                    modifier = Modifier.weight(1f),
                    accent = MaterialTheme.colorScheme.error
                )
                SummaryCard(
                    title = stringResource(R.string.home_card_expiry_warning),
                    value = "${uiState.expiringCount} 批次",
                    modifier = Modifier.weight(1f),
                    accent = MaterialTheme.colorScheme.tertiary
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(
                    title = stringResource(R.string.home_card_today_sales),
                    value = "¥ ${"%.2f".format(uiState.todayRevenue)}",
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = stringResource(R.string.home_card_month_profit),
                    value = "¥ ${"%.2f".format(uiState.monthProfit)}",
                    modifier = Modifier.weight(1f),
                    accent = MaterialTheme.colorScheme.primary
                )
            }
        }
        if (lowStock.isNotEmpty()) {
            item {
                Surface(
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("库存预警明细", style = MaterialTheme.typography.titleMedium)
                        lowStock.forEach { m ->
                            Text("· ${m.name}：当前 ${m.currentStock} ${m.unit} / 安全 ${m.safetyStock} ${m.unit}")
                        }
                    }
                }
            }
        }
        if (expiring.isNotEmpty()) {
            item {
                Surface(
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("7 日内临近保质期", style = MaterialTheme.typography.titleMedium)
                        val now = System.currentTimeMillis()
                        expiring.take(5).forEach { batch ->
                            val matName = lowStock.firstOrNull { it.id == batch.materialId }?.name
                                ?: "原料 #${batch.materialId}"
                            val days = batch.expiryDate?.let { (it - now) / (24 * 60 * 60 * 1000L) } ?: 0L
                            Text("· $matName：${batch.remainingQuantity}（剩 ${days} 天到期）")
                        }
                    }
                }
            }
        }
    }
}