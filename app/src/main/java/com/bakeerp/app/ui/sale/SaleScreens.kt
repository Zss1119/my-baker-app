package com.bakeerp.app.ui.sale

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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
private fun obtainVm(): SaleViewModel {
    val app = LocalContext.current.applicationContext as com.bakeerp.app.BakeErpApplication
    return viewModel(factory = SaleViewModelFactory(app))
}

private val PAYMENT_METHODS = listOf("现金", "微信", "支付宝", "其他")

// ===================== 列表 =====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleListScreen(onAdd: () -> Unit) {
    val vm = obtainVm()
    val list by vm.sales.collectAsStateWithLifecycle()
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.sale_list_title)) }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAdd, text = { Text(stringResource(R.string.action_add)) }, icon = {})
        }
    ) { padding ->
        if (list.isEmpty()) {
            EmptyHint("暂无销售记录", Modifier.padding(padding))
            return@Scaffold
        }
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)) {
            items(list, key = { it.id }) { s ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("${s.productName} × ${"%.2f".format(s.quantity)}", style = MaterialTheme.typography.titleMedium)
                        Text("${DateUtils.format(s.saleDate)}｜${s.customerName.ifBlank { "(散客)" }}｜${s.paymentMethod}｜¥ ${"%.2f".format(s.totalPrice)}",
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
fun SaleAddScreen(onBack: () -> Unit) {
    val vm = obtainVm()
    val form by vm.form.collectAsStateWithLifecycle()
    val customers by vm.customers.collectAsStateWithLifecycle()

    var paymentMenu by remember { mutableStateOf(false) }
    var customerMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sale_add_title)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = form.productName,
                onValueChange = { v -> vm.updateForm { it.copy(productName = v) } },
                label = { Text(stringResource(R.string.sale_field_product)) },
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = if (form.quantity == 0.0) "" else form.quantity.toString(),
                    onValueChange = { v ->
                        val parsed = v.filter { c -> c.isDigit() || c == '.' }.toDoubleOrNull() ?: 0.0
                        vm.updateForm { it.copy(quantity = parsed) }
                    },
                    label = { Text(stringResource(R.string.sale_field_quantity)) },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = if (form.unitPrice == 0.0) "" else form.unitPrice.toString(),
                    onValueChange = { v ->
                        val parsed = v.filter { c -> c.isDigit() || c == '.' }.toDoubleOrNull() ?: 0.0
                        vm.updateForm { it.copy(unitPrice = parsed) }
                    },
                    label = { Text(stringResource(R.string.sale_field_unit_price)) },
                    modifier = Modifier.weight(1f)
                )
            }
            Text("合计：¥ ${"%.2f".format(form.quantity * form.unitPrice)}", style = MaterialTheme.typography.titleMedium)

            ExposedDropdownMenuBox(expanded = customerMenu, onExpandedChange = { customerMenu = !customerMenu }) {
                OutlinedTextField(
                    value = form.customerName.ifBlank { "(散客)" },
                    onValueChange = { v -> vm.updateForm { it.copy(customerName = v) } },
                    label = { Text(stringResource(R.string.sale_field_customer)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerMenu) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                DropdownMenu(expanded = customerMenu, onDismissRequest = { customerMenu = false }) {
                    customers.forEach { c ->
                        DropdownMenuItem(text = { Text(c.name) }, onClick = {
                            vm.updateForm { it.copy(customerName = c.name) }
                            customerMenu = false
                        })
                    }
                }
            }

            ExposedDropdownMenuBox(expanded = paymentMenu, onExpandedChange = { paymentMenu = !paymentMenu }) {
                OutlinedTextField(
                    value = form.paymentMethod,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.sale_field_payment)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentMenu) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                DropdownMenu(expanded = paymentMenu, onDismissRequest = { paymentMenu = false }) {
                    PAYMENT_METHODS.forEach { p ->
                        DropdownMenuItem(text = { Text(p) }, onClick = {
                            vm.updateForm { it.copy(paymentMethod = p) }
                            paymentMenu = false
                        })
                    }
                }
            }

            OutlinedTextField(
                value = form.notes,
                onValueChange = { v -> vm.updateForm { it.copy(notes = v) } },
                label = { Text("备注") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { vm.submit { onBack() } },
                enabled = form.productName.isNotBlank() && form.quantity > 0.0,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.action_save)) }
        }
    }
}