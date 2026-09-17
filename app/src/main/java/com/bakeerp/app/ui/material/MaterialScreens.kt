package com.bakeerp.app.ui.material

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bakeerp.app.R
import com.bakeerp.app.data.database.entity.MaterialEntity
import com.bakeerp.app.data.database.entity.PurchaseRecordEntity
import com.bakeerp.app.ui.components.EmptyHint
import com.bakeerp.app.util.DateUtils

private fun obtainVm(): MaterialViewModel {
    val app = LocalContext.current.applicationContext as com.bakeerp.app.BakeErpApplication
    return viewModel(factory = MaterialViewModel.Factory(app))
}

// ===================== 列表 =====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialListScreen(onItemClick: (Long) -> Unit, onAdd: () -> Unit) {
    val vm = obtainVm()
    val list by vm.materials.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.material_list_title)) }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAdd, text = { Text(stringResource(R.string.action_add)) }, icon = {})
        }
    ) { padding ->
        if (list.isEmpty()) {
            EmptyHint("暂无原材料，点击右下角按钮新增", Modifier.padding(padding))
            return@Scaffold
        }
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)) {
            items(list, key = { it.id }) { m ->
                MaterialRow(m, onClick = { onItemClick(m.id) })
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun MaterialRow(m: MaterialEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(m.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text("${"%.2f".format(m.currentStock)} ${m.unit}", style = MaterialTheme.typography.bodyMedium)
            }
            if (m.safetyStock > 0.0 && m.currentStock < m.safetyStock) {
                Text(
                    "低于安全库存（${m.safetyStock} ${m.unit}）",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// ===================== 详情 =====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialDetailScreen(materialId: Long, onBack: () -> Unit, onEdit: () -> Unit, onAddPurchase: () -> Unit) {
    val vm = obtainVm()
    LaunchedEffect(materialId) { vm.loadMaterial(materialId) }
    val entity by vm.editing.collectAsStateWithLifecycle()
    val history by vm.observeHistory(materialId).collectAsStateWithLifecycle(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.material_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) }
                },
                actions = {
                    IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = null) }
                }
            )
        }
    ) { padding ->
        val m = entity ?: return@Scaffold
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DetailRow(stringResource(R.string.material_field_name), m.name)
            DetailRow(stringResource(R.string.material_field_unit), m.unit)
            DetailRow(stringResource(R.string.material_field_stock), "${m.currentStock} ${m.unit}")
            DetailRow(stringResource(R.string.material_field_safety_stock), "${m.safetyStock} ${m.unit}")
            DetailRow(stringResource(R.string.material_field_unit_price), "¥ ${"%.2f".format(m.pricePerUnit)} / ${m.unit}")
            if (m.notes.isNotBlank()) DetailRow(stringResource(R.string.material_field_notes), m.notes)
            Button(onClick = onAddPurchase, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.material_add_purchase)) }
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.material_purchase_history), style = MaterialTheme.typography.titleMedium)
            if (history.isEmpty()) {
                Text("暂无采购记录", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            } else {
                history.forEach { p ->
                    Surface(tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Column(Modifier.padding(8.dp)) {
                            Text("${DateUtils.format(p.purchaseDate)}｜${p.quantity} ${m.unit}（剩 ${p.remainingQuantity}）｜¥ ${"%.2f".format(p.totalPrice)}")
                            if (p.supplier.isNotBlank()) Text("供应商：${p.supplier}", style = MaterialTheme.typography.bodyMedium)
                            p.expiryDate?.let { Text("到期：${DateUtils.formatDate(it)}", style = MaterialTheme.typography.bodyMedium) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.width(96.dp), style = MaterialTheme.typography.labelLarge)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

// ===================== 新增/编辑 =====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialEditScreen(materialId: Long, onBack: () -> Unit) {
    val vm = obtainVm()
    LaunchedEffect(materialId) { vm.loadMaterial(materialId) }
    val initial by vm.editing.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("g") }
    var stock by remember { mutableStateOf("0") }
    var safety by remember { mutableStateOf("0") }
    var price by remember { mutableStateOf("0") }
    var notes by remember { mutableStateOf("") }
    LaunchedEffect(initial) {
        initial?.let {
            name = it.name
            unit = it.unit
            stock = it.currentStock.toString()
            safety = it.safetyStock.toString()
            price = it.pricePerUnit.toString()
            notes = it.notes
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (materialId <= 0L) R.string.material_add_title else R.string.material_edit_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.material_field_name)) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text(stringResource(R.string.material_field_unit)) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = stock, onValueChange = { stock = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text(stringResource(R.string.material_field_stock)) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = safety, onValueChange = { safety = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text(stringResource(R.string.material_field_safety_stock)) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = price, onValueChange = { price = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text(stringResource(R.string.material_field_unit_price)) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text(stringResource(R.string.material_field_notes)) }, modifier = Modifier.fillMaxWidth())
            Button(
                onClick = {
                    val entity = MaterialEntity(
                        id = materialId,
                        name = name.trim(),
                        unit = unit.trim().ifEmpty { "g" },
                        currentStock = stock.toDoubleOrNull() ?: 0.0,
                        safetyStock = safety.toDoubleOrNull() ?: 0.0,
                        pricePerUnit = price.toDoubleOrNull() ?: 0.0,
                        notes = notes.trim(),
                        createdAt = initial?.createdAt ?: System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    vm.saveMaterial(entity) { onBack() }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.action_save)) }
        }
    }
}

// ===================== 新增采购 =====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialAddPurchaseScreen(materialId: Long, onBack: () -> Unit) {
    val vm = obtainVm()
    LaunchedEffect(materialId) { vm.loadMaterial(materialId) }
    val entity by vm.editing.collectAsStateWithLifecycle()

    var qty by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var supplier by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.material_add_purchase)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) }
                }
            )
        }
    ) { padding ->
        val m = entity ?: return@Scaffold
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("为 ${m.name} 录入采购记录", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(value = qty, onValueChange = { qty = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("数量（${m.unit}）") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = price, onValueChange = { price = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("单价（¥ / ${m.unit}）") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = supplier, onValueChange = { supplier = it }, label = { Text("供应商") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = expiry, onValueChange = { expiry = it }, label = { Text("保质期到期日（yyyy-MM-dd，可空）") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("备注") }, modifier = Modifier.fillMaxWidth())
            Button(
                onClick = {
                    val q = qty.toDoubleOrNull() ?: 0.0
                    if (q <= 0.0) return@Button
                    val expiryTs = if (expiry.isBlank()) null else parseDateOrNull(expiry)
                    val record = PurchaseRecordEntity(
                        materialId = m.id,
                        quantity = q,
                        unitPrice = price.toDoubleOrNull() ?: 0.0,
                        totalPrice = q * (price.toDoubleOrNull() ?: 0.0),
                        supplier = supplier.trim(),
                        purchaseDate = System.currentTimeMillis(),
                        expiryDate = expiryTs,
                        remainingQuantity = q,
                        notes = notes.trim()
                    )
                    vm.addPurchase(record) { onBack() }
                },
                enabled = qty.toDoubleOrNull()?.let { it > 0.0 } == true,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.action_save)) }
        }
    }
}

private fun parseDateOrNull(s: String): Long? = try {
    val parts = s.split("-")
    if (parts.size != 3) null
    else {
        val cal = java.util.Calendar.getInstance()
        cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt(), 23, 59, 59)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }
} catch (e: Exception) { null }