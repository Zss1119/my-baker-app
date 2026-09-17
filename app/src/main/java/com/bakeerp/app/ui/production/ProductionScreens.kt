package com.bakeerp.app.ui.production

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import com.bakeerp.app.ui.components.EmptyHint
import com.bakeerp.app.util.DateUtils

@Composable
private fun obtainVm(): ProductionViewModel {
    val app = LocalContext.current.applicationContext as com.bakeerp.app.BakeErpApplication
    return viewModel(factory = ProductionViewModel.Factory(app))
}

// ===================== 列表 =====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductionListScreen(onAdd: () -> Unit) {
    val vm = obtainVm()
    val list by vm.productions.collectAsStateWithLifecycle()
    val recipes by vm.recipes.collectAsStateWithLifecycle()
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.production_list_title)) }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAdd, text = { Text(stringResource(R.string.action_add)) }, icon = {})
        }
    ) { padding ->
        if (list.isEmpty()) {
            EmptyHint("暂无制作记录", Modifier.padding(padding))
            return@Scaffold
        }
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)) {
            items(list, key = { it.id }) { p ->
                val r = recipes.firstOrNull { it.id == p.recipeId }
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(r?.name ?: "(已删除配方)", style = MaterialTheme.typography.titleMedium)
                        Text("${DateUtils.format(p.productionDate)}｜批次 ${p.batchCount}｜产出 ${p.producedQuantity}｜原料成本 ¥ ${"%.2f".format(p.totalMaterialCost)}",
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
                HorizontalDivider()
            }
        }
    }
}

// ===================== 新增 =====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductionAddScreen(onBack: () -> Unit) {
    val vm = obtainVm()
    val recipes by vm.recipes.collectAsStateWithLifecycle()
    val selectedId by vm.selectedRecipeId.collectAsStateWithLifecycle()
    val batch by vm.batchCount.collectAsStateWithLifecycle()
    val produced by vm.producedQuantity.collectAsStateWithLifecycle()
    val items by vm.previewItems.collectAsStateWithLifecycle()
    val totalCost by vm.totalCost.collectAsStateWithLifecycle()

    var menuExpanded by remember { mutableStateOf(false) }
    var batchInput by remember(batch.toString()) { mutableStateOf(batch.toString()) }
    var producedInput by remember(produced.toString()) { mutableStateOf(if (produced == 0.0) "" else produced.toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.production_add_title)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (recipes.isEmpty()) {
                Text("请先在「配方」中创建配方", color = MaterialTheme.colorScheme.error)
                return@Scaffold
            }
            ExposedDropdownMenuBox(expanded = menuExpanded, onExpandedChange = { menuExpanded = !menuExpanded }) {
                OutlinedTextField(
                    value = recipes.firstOrNull { it.id == selectedId }?.name ?: "选择配方",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.production_field_recipe)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    recipes.forEach { r ->
                        DropdownMenuItem(text = { Text(r.name) }, onClick = {
                            vm.setRecipe(r.id)
                            menuExpanded = false
                        })
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = batchInput,
                    onValueChange = {
                        batchInput = it.filter { c -> c.isDigit() || c == '.' }
                        batchInput.toDoubleOrNull()?.let { vm.setBatch(it) }
                    },
                    label = { Text(stringResource(R.string.production_field_batch)) },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = producedInput,
                    onValueChange = {
                        producedInput = it.filter { c -> c.isDigit() || c == '.' }
                        producedInput.toDoubleOrNull()?.let { vm.setProduced(it) }
                    },
                    label = { Text(stringResource(R.string.production_field_produced)) },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.production_preview_consumption), style = MaterialTheme.typography.titleMedium)
            if (items.isEmpty()) {
                Text("选择配方并输入批次量后显示预览", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            } else {
                items.forEach { ci ->
                    Surface(tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("· ${ci.materialName}：${"%.2f".format(ci.quantity)} ${ci.unit}", modifier = Modifier.weight(1f))
                            Text("¥ ${"%.2f".format(ci.subtotalCost)}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                Text("原料总成本：¥ ${"%.2f".format(totalCost)}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 4.dp))
            }
            Button(
                onClick = { vm.confirmProduction { onBack() } },
                enabled = selectedId > 0L && items.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.action_confirm)) }
        }
    }
}