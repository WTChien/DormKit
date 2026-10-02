package com.dormkit.app.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dormkit.app.model.Category
import com.dormkit.app.model.Item
import com.dormkit.app.model.ItemWithCategory
import com.dormkit.app.model.StorageLocation
import com.dormkit.app.ui.components.EmptyState
import com.dormkit.app.ui.components.LocationSwitchFab
import com.dormkit.app.ui.components.ScreenTitle
import com.dormkit.app.viewmodel.MainViewModel

@Composable
fun InventoryScreen(viewModel: MainViewModel) {
    val items by viewModel.visibleItems.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val location by viewModel.storageLocation.collectAsStateWithLifecycle()
    var editorItem by remember { mutableStateOf<ItemWithCategory?>(null) }
    var editorOpen by remember { mutableStateOf(false) }
    var deleteItem by remember { mutableStateOf<ItemWithCategory?>(null) }

    Scaffold(
        topBar = {
            Column {
                ScreenTitle(title = location.inventoryTitle, subtitle = if (filter.lowOnly) "只顯示庫存不足" else "${items.size} 個物品")
                OutlinedTextField(
                    value = filter.search,
                    onValueChange = viewModel::setSearch,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    placeholder = { Text("搜尋物品") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filter.categoryId == null && !filter.lowOnly,
                        onClick = { viewModel.clearInventoryFilters() },
                        label = { Text("全部") }
                    )
                    FilterChip(
                        selected = filter.lowOnly,
                        onClick = { viewModel.showLowStockOnly(!filter.lowOnly) },
                        label = { Text("庫存不足") },
                        leadingIcon = { Icon(Icons.Default.Warning, null) }
                    )
                    categories.forEach { category ->
                        FilterChip(
                            selected = filter.categoryId == category.id,
                            onClick = {
                                viewModel.showLowStockOnly(false)
                                viewModel.setCategoryFilter(if (filter.categoryId == category.id) null else category.id)
                            },
                            label = { Text("${category.icon} ${category.name}") }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LocationSwitchFab(
                    location = location,
                    onClick = { viewModel.setStorageLocation(location.other) }
                )
                FloatingActionButton(onClick = { editorItem = null; editorOpen = true }) {
                    Icon(Icons.Default.Add, contentDescription = "新增物品")
                }
            }
        }
    ) { padding ->
        if (items.isEmpty()) {
            Column(Modifier.padding(padding)) {
                EmptyState(
                    if (filter.search.isNotBlank() || filter.categoryId != null || filter.lowOnly) "找不到符合的物品" else "還沒有庫存物品",
                    if (filter.lowOnly) "目前沒有需要補貨的物品" else "按右下角的＋新增第一個物品"
                )
            }
        } else {
            Column(
                modifier = Modifier.padding(padding).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { item ->
                    InventoryRow(
                        item = item,
                        onDecrease = { viewModel.adjustItem(item.id, -1) },
                        onIncrease = { viewModel.adjustItem(item.id, 1) },
                        onEdit = { editorItem = item; editorOpen = true },
                        onDelete = { deleteItem = item }
                    )
                }
                Spacer(Modifier.height(88.dp))
            }
        }
    }

    if (editorOpen) {
        ItemEditorDialog(
            existing = editorItem,
            categories = categories,
            defaultLocation = location,
            onDismiss = { editorOpen = false },
            onSave = { viewModel.saveItem(it); editorOpen = false }
        )
    }
    deleteItem?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteItem = null },
            title = { Text("刪除「${target.name}」？") },
            text = { Text("刪除後無法復原。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteItem(target.asItem())
                    deleteItem = null
                }) { Text("刪除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { deleteItem = null }) { Text("取消") } }
        )
    }
}

@Composable
private fun InventoryRow(
    item: ItemWithCategory,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (item.isLowStock) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(item.categoryIcon ?: "📦", style = MaterialTheme.typography.headlineSmall)
            Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.name, fontWeight = FontWeight.SemiBold)
                    if (item.isLowStock) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "庫存不足",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 5.dp)
                        )
                    }
                }
                Text(
                    "${item.categoryName ?: "未分類"} · 最低 ${item.minimumQuantity} ${item.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FilledTonalIconButton(onClick = onDecrease, enabled = item.quantity > 0) {
                Icon(Icons.Default.Remove, contentDescription = "減少")
            }
            Text(
                item.quantity.toString(),
                modifier = Modifier.padding(horizontal = 10.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (item.isLowStock) MaterialTheme.colorScheme.error else Color.Unspecified
            )
            FilledTonalIconButton(onClick = onIncrease) { Icon(Icons.Default.Add, contentDescription = "增加") }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "編輯") }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "刪除") }
        }
    }
}

@Composable
private fun ItemEditorDialog(
    existing: ItemWithCategory?,
    categories: List<Category>,
    defaultLocation: StorageLocation,
    onDismiss: () -> Unit,
    onSave: (Item) -> Unit
) {
    var name by remember(existing?.id) { mutableStateOf(existing?.name.orEmpty()) }
    var categoryId by remember(existing?.id, categories) { mutableStateOf(existing?.categoryId ?: categories.firstOrNull()?.id) }
    var quantity by remember(existing?.id) { mutableStateOf(existing?.quantity?.toString() ?: "1") }
    var unit by remember(existing?.id) { mutableStateOf(existing?.unit ?: "個") }
    var minimum by remember(existing?.id) { mutableStateOf(existing?.minimumQuantity?.toString() ?: "1") }
    var note by remember(existing?.id) { mutableStateOf(existing?.note.orEmpty()) }
    var location by remember(existing?.id) { mutableStateOf(existing?.location ?: defaultLocation) }
    val valid = name.isNotBlank() && quantity.toIntOrNull() != null && minimum.toIntOrNull() != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "新增物品" else "編輯物品") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("物品名稱") }, singleLine = true)
                Text("存放位置", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StorageLocation.entries.forEach { value ->
                        FilterChip(
                            selected = location == value,
                            onClick = { location = value },
                            label = { Text(value.label) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Text("分類", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = categoryId == category.id,
                            onClick = { categoryId = category.id },
                            label = { Text("${category.icon} ${category.name}") }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        quantity, { quantity = it.filter(Char::isDigit) },
                        Modifier.weight(1f), label = { Text("數量") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(unit, { unit = it }, Modifier.weight(1f), label = { Text("單位") }, singleLine = true)
                }
                OutlinedTextField(
                    minimum, { minimum = it.filter(Char::isDigit) },
                    label = { Text("最低庫存數量") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(note, { note = it }, label = { Text("備註（選填）") }, minLines = 2)
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = {
                    onSave(
                        Item(
                            id = existing?.id ?: 0,
                            name = name.trim(),
                            categoryId = categoryId,
                            quantity = quantity.toIntOrNull()?.coerceAtLeast(0) ?: 0,
                            unit = unit.trim().ifBlank { "個" },
                            minimumQuantity = minimum.toIntOrNull()?.coerceAtLeast(0) ?: 0,
                            note = note.trim(),
                            location = location
                        )
                    )
                }
            ) { Text("儲存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

private fun ItemWithCategory.asItem() = Item(
    id = id,
    name = name,
    categoryId = categoryId,
    quantity = quantity,
    unit = unit,
    minimumQuantity = minimumQuantity,
    note = note,
    location = location
)
