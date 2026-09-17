package com.bakeerp.app.ui.recipe

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import com.bakeerp.app.data.database.entity.RecipeEntity
import com.bakeerp.app.ui.components.EmptyHint

private fun obtainVm(): RecipeViewModel {
    val app = LocalContext.current.applicationContext as com.bakeerp.app.BakeErpApplication
    return viewModel(factory = RecipeViewModel.Factory(app))
}

// ===================== 列表 =====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(onItemClick: (Long) -> Unit, onAdd: () -> Unit) {
    val vm = obtainVm()
    val list by vm.recipes.collectAsStateWithLifecycle()
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.recipe_list_title)) }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAdd, text = { Text(stringResource(R.string.action_add)) }, icon = {})
        }
    ) { padding ->
        if (list.isEmpty()) {
            EmptyHint("暂无配方，点击右下角按钮新增", Modifier.padding(padding))
            return@Scaffold
        }
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)) {
            items(list, key = { it.id }) { r ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onItemClick(r.id) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(r.name, style = MaterialTheme.typography.titleMedium)
                        Text("产出：${r.productName} ${r.expectedOutput} ${r.outputUnit}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                HorizontalDivider()
            }
        }
    }
}

// ===================== 详情 =====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(recipeId: Long, onBack: () -> Unit) {
    val vm = obtainVm()
    LaunchedEffect(recipeId) { vm.loadRecipe(recipeId) }
    val editing by vm.editing.collectAsStateWithLifecycle()
    val items by vm.items.collectAsStateWithLifecycle()
    val materials by vm.materials.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(editing?.name ?: stringResource(R.string.recipe_list_title)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) } }
            )
        }
    ) { padding ->
        val r = editing ?: return@Scaffold
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("产出：${r.productName} ${r.expectedOutput} ${r.outputUnit}", style = MaterialTheme.typography.titleMedium)
            if (r.description.isNotBlank()) Text(r.description, style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.recipe_items), style = MaterialTheme.typography.titleMedium)
            items.forEach { item ->
                val mat = materials.firstOrNull { it.id == item.materialId }
                Surface(tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "· ${mat?.name ?: "(未知原料)"}：${item.quantityPerUnit} ${mat?.unit ?: ""}",
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

// ===================== 新增/编辑 =====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditScreen(recipeId: Long, onBack: () -> Unit) {
    val vm = obtainVm()
    LaunchedEffect(recipeId) { vm.loadRecipe(recipeId) }
    val initial by vm.editing.collectAsStateWithLifecycle()
    val items by vm.items.collectAsStateWithLifecycle()
    val materials by vm.materials.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var product by remember { mutableStateOf("") }
    var expected by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("个") }
    var description by remember { mutableStateOf("") }

    LaunchedEffect(initial) {
        initial?.let {
            name = it.name
            product = it.productName
            expected = it.expectedOutput.toString()
            unit = it.outputUnit
            description = it.description
        }
    }

    // 新增原料临时态
    var pickMaterialId by remember { mutableStateOf<Long?>(null) }
    var pickQty by remember { mutableStateOf("") }
    var materialMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (recipeId <= 0L) R.string.recipe_add_title else R.string.recipe_edit_title)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.recipe_field_name)) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = product, onValueChange = { product = it }, label = { Text(stringResource(R.string.recipe_field_product)) }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = expected, onValueChange = { expected = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text(stringResource(R.string.recipe_field_expected_output)) }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text(stringResource(R.string.recipe_field_output_unit)) }, modifier = Modifier.weight(1f))
            }
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text(stringResource(R.string.recipe_field_description)) }, modifier = Modifier.fillMaxWidth())

            Text(stringResource(R.string.recipe_items), style = MaterialTheme.typography.titleMedium)
            items.forEachIndexed { idx, it ->
                val mat = materials.firstOrNull { m -> m.id == it.materialId }
                Surface(tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("${mat?.name ?: "(未知)"}：${it.quantityPerUnit} ${mat?.unit ?: ""}", modifier = Modifier.weight(1f))
                        IconButton(onClick = { vm.removeItem(idx) }) { Icon(Icons.Filled.Delete, contentDescription = null) }
                    }
                }
            }

            if (materials.isNotEmpty()) {
                ExposedDropdownMenuBox(expanded = materialMenuExpanded, onExpandedChange = { materialMenuExpanded = !materialMenuExpanded }) {
                    OutlinedTextField(
                        value = materials.firstOrNull { it.id == pickMaterialId }?.name ?: "选择原料",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("原料") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = materialMenuExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    DropdownMenu(expanded = materialMenuExpanded, onDismissRequest = { materialMenuExpanded = false }) {
                        materials.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m.name) },
                                onClick = { pickMaterialId = m.id; materialMenuExpanded = false }
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = pickQty, onValueChange = { pickQty = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("用量") }, modifier = Modifier.weight(1f))
                    Button(
                        onClick = {
                            val id = pickMaterialId ?: return@Button
                            val q = pickQty.toDoubleOrNull() ?: return@Button
                            vm.addItem(id, q)
                            pickQty = ""
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.recipe_add_item)) }
                }
            } else {
                Text("请先在「原材料」录入原料", color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    val entity = RecipeEntity(
                        id = recipeId,
                        name = name.trim(),
                        productName = product.trim(),
                        expectedOutput = expected.toDoubleOrNull() ?: 1.0,
                        outputUnit = unit.trim().ifEmpty { "个" },
                        description = description.trim(),
                        createdAt = initial?.createdAt ?: System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    vm.setRecipe(entity)
                    vm.saveRecipe { onBack() }
                },
                enabled = name.isNotBlank() && product.isNotBlank() && items.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.action_save)) }
        }
    }
}