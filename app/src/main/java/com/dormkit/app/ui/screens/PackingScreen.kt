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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dormkit.app.model.PackingDirection
import com.dormkit.app.model.PackingItem
import com.dormkit.app.ui.components.EmptyState
import com.dormkit.app.ui.components.ScreenTitle
import com.dormkit.app.viewmodel.MainViewModel

@Composable
fun PackingScreen(viewModel: MainViewModel) {
    val allItems by viewModel.packingItems.collectAsStateWithLifecycle()
    var direction by remember { mutableStateOf(PackingDirection.DORM_TO_HOME) }
    val items = allItems.filter { it.direction == direction }
    var editorItem by remember { mutableStateOf<PackingItem?>(null) }
    var editorOpen by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<PackingItem?>(null) }

    Scaffold(
        topBar = {
            Column {
                ScreenTitle("回家 / 回宿舍清單", "${items.count { it.checked }} / ${items.size} 已完成")
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PackingDirection.entries.forEach { value ->
                        FilterChip(
                            selected = direction == value,
                            onClick = { direction = value },
                            label = { Text(value.label) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editorItem = null; editorOpen = true }) {
                Icon(Icons.Default.Add, contentDescription = "新增清單項目")
            }
        }
    ) { padding ->
        if (items.isEmpty()) {
            Column(Modifier.padding(padding)) {
                EmptyState("清單還是空的", "按右下角的＋新增要攜帶的物品")
            }
        } else {
            Column(
                Modifier.padding(padding).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { item ->
                    PackingRow(
                        item = item,
                        onChecked = { viewModel.setPackingChecked(item.id, it) },
                        onDecrease = { viewModel.adjustPacking(item.id, -1) },
                        onIncrease = { viewModel.adjustPacking(item.id, 1) },
                        onEdit = { editorItem = item; editorOpen = true },
                        onDelete = { deleting = item }
                    )
                }
                Spacer(Modifier.height(88.dp))
            }
        }
    }

    if (editorOpen) {
        PackingEditorDialog(
            existing = editorItem,
            defaultDirection = direction,
            onDismiss = { editorOpen = false },
            onSave = { viewModel.savePackingItem(it); editorOpen = false }
        )
    }
    deleting?.let { item ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("刪除「${item.name}」？") },
            text = { Text("刪除後無法復原。") },
            confirmButton = {
                TextButton(onClick = { viewModel.deletePackingItem(item); deleting = null }) {
                    Text("刪除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("取消") } }
        )
    }
}

@Composable
private fun PackingRow(
    item: PackingItem,
    onChecked: (Boolean) -> Unit,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.alpha(if (item.checked) 0.55f else 1f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = item.checked, onCheckedChange = onChecked)
            Column(Modifier.weight(1f)) {
                Text(
                    item.name,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (item.checked) TextDecoration.LineThrough else TextDecoration.None
                )
                if (item.note.isNotBlank()) {
                    Text(item.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            FilledTonalIconButton(onClick = onDecrease, enabled = item.quantity > 1) {
                Icon(Icons.Default.Remove, contentDescription = "減少")
            }
            Text("${item.quantity} ${item.unit}", Modifier.padding(horizontal = 8.dp))
            FilledTonalIconButton(onClick = onIncrease) { Icon(Icons.Default.Add, contentDescription = "增加") }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "編輯") }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "刪除") }
        }
    }
}

@Composable
private fun PackingEditorDialog(
    existing: PackingItem?,
    defaultDirection: PackingDirection,
    onDismiss: () -> Unit,
    onSave: (PackingItem) -> Unit
) {
    var name by remember(existing?.id) { mutableStateOf(existing?.name.orEmpty()) }
    var direction by remember(existing?.id) { mutableStateOf(existing?.direction ?: defaultDirection) }
    var quantity by remember(existing?.id) { mutableStateOf(existing?.quantity?.toString() ?: "1") }
    var unit by remember(existing?.id) { mutableStateOf(existing?.unit ?: "個") }
    var note by remember(existing?.id) { mutableStateOf(existing?.note.orEmpty()) }
    val valid = name.isNotBlank() && (quantity.toIntOrNull() ?: 0) > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "新增清單項目" else "編輯清單項目") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("名稱") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PackingDirection.entries.forEach { value ->
                        FilterChip(
                            selected = direction == value,
                            onClick = { direction = value },
                            label = { Text(value.label) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        quantity, { quantity = it.filter(Char::isDigit) }, Modifier.weight(1f),
                        label = { Text("數量") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(unit, { unit = it }, Modifier.weight(1f), label = { Text("單位") }, singleLine = true)
                }
                OutlinedTextField(note, { note = it }, label = { Text("備註（選填）") })
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = {
                    onSave(
                        PackingItem(
                            id = existing?.id ?: 0,
                            name = name.trim(),
                            direction = direction,
                            quantity = quantity.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                            unit = unit.trim().ifBlank { "個" },
                            checked = existing?.checked ?: false,
                            note = note.trim()
                        )
                    )
                }
            ) { Text("儲存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
