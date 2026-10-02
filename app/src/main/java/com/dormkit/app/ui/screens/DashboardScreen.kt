package com.dormkit.app.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dormkit.app.model.PackingDirection
import com.dormkit.app.model.StorageLocation
import com.dormkit.app.ui.components.LocationSwitchFab
import com.dormkit.app.ui.components.ScreenTitle
import com.dormkit.app.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onInventory: (Boolean) -> Unit,
    onPacking: () -> Unit,
    onLaundry: () -> Unit
) {
    val items by viewModel.locationItems.collectAsStateWithLifecycle()
    val location by viewModel.storageLocation.collectAsStateWithLifecycle()
    val packingItems by viewModel.packingItems.collectAsStateWithLifecycle()
    val timer by viewModel.laundryTimer.collectAsStateWithLifecycle()
    val lowStock = items.filter { it.isLowStock }
    val toDorm = packingItems.filter { it.direction == PackingDirection.HOME_TO_DORM && !it.checked }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(timer?.endTime, timer?.active) {
        while (timer?.active == true && timer!!.endTime > now) {
            now = System.currentTimeMillis()
            delay(1_000)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        ) {
            ScreenTitle(title = "DormKit", subtitle = "${location.label}生活管家")
            Spacer(Modifier.height(8.dp))

        Card(
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().clickable { onInventory(true) },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(
                        if (lowStock.isEmpty()) "庫存都很充足" else "有 ${lowStock.size} 個物品數量過低",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        if (lowStock.isEmpty()) "目前沒有需要補貨的物品" else "點一下查看需要補貨的物品",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Icon(Icons.Default.ChevronRight, contentDescription = "查看")
            }
        }

        Spacer(Modifier.height(14.dp))
        Card(
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Row(
                Modifier.fillMaxWidth().clickable(onClick = onPacking).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Checklist, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Text("下次回宿舍要帶", Modifier.weight(1f).padding(horizontal = 12.dp), fontWeight = FontWeight.Bold)
                Icon(Icons.Default.ChevronRight, contentDescription = "查看完整清單")
            }
            if (toDorm.isEmpty()) {
                Text("清單已完成，或還沒有新增項目", Modifier.padding(start = 20.dp, end = 20.dp, bottom = 18.dp))
            } else {
                toDorm.take(5).forEach { item ->
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = item.checked,
                            onCheckedChange = { viewModel.setPackingChecked(item.id, it) }
                        )
                        Text("${item.name}${if (item.quantity > 1) " × ${item.quantity}" else ""}")
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        if (timer?.active == true && timer!!.endTime > now) {
            Spacer(Modifier.height(14.dp))
            Card(
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().clickable(onClick = onLaundry),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalLaundryService, null, Modifier.size(30.dp))
                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text("洗衣機運轉中", fontWeight = FontWeight.Bold)
                        Text("剩餘 ${formatRemaining(timer!!.endTime - now)} · ${formatFinish(timer!!.endTime)} 完成")
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = "查看計時器")
                }
            }
        }

        Text(
            "快速功能",
            modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 10.dp),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickAction(
                Icons.Default.Inventory2,
                location.inventoryTitle,
                "管理${location.label}的物品數量",
                Modifier.testTag("quick_inventory")
            ) { onInventory(false) }
            QuickAction(
                Icons.Default.Checklist,
                "回家清單",
                "帶回家／回宿舍",
                Modifier.testTag("quick_packing")
            ) { onPacking() }
            QuickAction(
                Icons.Default.LocalLaundryService,
                "洗衣計時器",
                "到時通知你",
                Modifier.testTag("quick_laundry")
            ) { onLaundry() }
        }
            Spacer(Modifier.height(88.dp))
        }

        LocationSwitchFab(
            location = location,
            onClick = { viewModel.setStorageLocation(location.other) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )
    }
}

@Composable
private fun QuickAction(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(modifier = modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "開啟")
        }
    }
}

internal fun formatRemaining(milliseconds: Long): String {
    val totalSeconds = milliseconds.coerceAtLeast(0) / 1_000
    val hours = totalSeconds / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds)
    else "%02d:%02d".format(minutes, seconds)
}

internal fun formatFinish(time: Long): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(time))
