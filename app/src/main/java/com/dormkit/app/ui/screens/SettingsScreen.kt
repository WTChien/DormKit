package com.dormkit.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dormkit.app.BuildConfig
import com.dormkit.app.model.Category
import com.dormkit.app.ui.components.ScreenTitle
import com.dormkit.app.viewmodel.MainViewModel

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val darkMode by viewModel.darkMode.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<Category?>(null) }
    var editorOpen by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<Category?>(null) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenTitle("設定", "調整 DormKit 的使用方式")
        Text("顯示", Modifier.padding(horizontal = 20.dp, vertical = 8.dp), fontWeight = FontWeight.Bold)
        Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DarkMode, null, tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text("深色模式", fontWeight = FontWeight.SemiBold)
                    Text("在較暗的環境減少螢幕亮度", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = darkMode, onCheckedChange = viewModel::setDarkMode)
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, top = 24.dp, end = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("分類管理", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            IconButton(onClick = { editing = null; editorOpen = true }) {
                Icon(Icons.Default.Add, contentDescription = "新增分類")
            }
        }
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.forEach { category ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(category.icon, style = MaterialTheme.typography.headlineSmall)
                        Text(category.name, Modifier.weight(1f).padding(horizontal = 12.dp), fontWeight = FontWeight.SemiBold)
                        IconButton(onClick = { editing = category; editorOpen = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "編輯分類")
                        }
                        IconButton(onClick = { deleting = category }) {
                            Icon(Icons.Default.Delete, contentDescription = "刪除分類")
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "DormKit ${BuildConfig.VERSION_NAME} · 所有資料只儲存在這台裝置",
            Modifier.align(Alignment.CenterHorizontally).padding(20.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (editorOpen) {
        CategoryEditorDialog(
            existing = editing,
            onDismiss = { editorOpen = false },
            onSave = { viewModel.saveCategory(it); editorOpen = false }
        )
    }
    deleting?.let { category ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("刪除「${category.name}」？") },
            text = { Text("這個分類中的物品會保留，並改為未分類。") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteCategory(category); deleting = null }) {
                    Text("刪除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("取消") } }
        )
    }
}

@Composable
private fun CategoryEditorDialog(
    existing: Category?,
    onDismiss: () -> Unit,
    onSave: (Category) -> Unit
) {
    var name by remember(existing?.id) { mutableStateOf(existing?.name.orEmpty()) }
    var icon by remember(existing?.id) { mutableStateOf(existing?.icon ?: "📦") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "新增分類" else "編輯分類") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("分類名稱") }, singleLine = true)
                OutlinedTextField(icon, { icon = it }, label = { Text("圖示（Emoji）") }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    onSave(Category(existing?.id ?: 0, name.trim(), icon.trim().ifBlank { "📦" }))
                }
            ) { Text("儲存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
