package com.bakeerp.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bakeerp.app.BuildConfig
import com.bakeerp.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val app = LocalContext.current.applicationContext as com.bakeerp.app.BakeErpApplication
    val vm: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(app))
    val reminder by vm.reminderEnabled.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_title)) }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            Surface(tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.settings_reminder), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.settings_reminder_desc), style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(checked = reminder, onCheckedChange = { vm.toggleReminder(it) })
                }
            }

            HorizontalDivider()

            // 占位：导出/导入/清空功能后续接 Storage Access Framework
            Button(onClick = { /* TODO: 导出 JSON */ }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.settings_export))
            }
            Button(onClick = { /* TODO: 导入 JSON */ }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.settings_import))
            }
            Button(
                onClick = { /* TODO: 清空数据 */ },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.settings_clear), color = MaterialTheme.colorScheme.error) }

            HorizontalDivider()

            Surface(tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(stringResource(R.string.settings_about), style = MaterialTheme.typography.titleMedium)
                    Text("${stringResource(R.string.settings_about_version)}：${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodyMedium)
                    Text("数据本地存储，离线可用，定期导出 JSON 备份。", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}