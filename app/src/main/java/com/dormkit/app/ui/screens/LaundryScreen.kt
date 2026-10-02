package com.dormkit.app.ui.screens

import android.app.AlarmManager
import android.content.Intent
import android.os.Build
import android.provider.Settings
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
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dormkit.app.ui.components.ScreenTitle
import com.dormkit.app.viewmodel.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun LaundryScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val alarmManager = remember { context.getSystemService(AlarmManager::class.java) }
    val canScheduleExactAlarm = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
    val timer by viewModel.laundryTimer.collectAsStateWithLifecycle()
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var customDialog by remember { mutableStateOf(false) }
    val isRunning = timer?.active == true && (timer?.endTime ?: 0) > now

    LaunchedEffect(timer?.endTime, timer?.active) {
        now = System.currentTimeMillis()
        while (timer?.active == true && (timer?.endTime ?: 0) > now) {
            delay(1_000)
            now = System.currentTimeMillis()
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenTitle("洗衣計時器", "時間到會發送通知", onBack = onBack)
        if (isRunning) {
            val current = timer!!
            val duration = (current.endTime - current.startTime).coerceAtLeast(1)
            val remaining = (current.endTime - now).coerceAtLeast(0)
            val progress = (remaining.toFloat() / duration).coerceIn(0f, 1f)
            Card(
                Modifier.padding(16.dp).fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.LocalLaundryService, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(16.dp))
                    Text("洗衣機", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(formatRemaining(remaining), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                    Text("預計 ${formatFinish(current.endTime)} 完成")
                    Spacer(Modifier.height(18.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(20.dp))
                    OutlinedButton(onClick = viewModel::cancelLaundry) { Text("提前取消") }
                }
            }
        } else {
            Card(Modifier.padding(16.dp).fillMaxWidth()) {
                Column(
                    Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.NotificationsActive, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(12.dp))
                    Text("開始一個洗衣計時器", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("即使 App 在背景，仍會依實際結束時間提醒你。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        if (!canScheduleExactAlarm) {
            Card(
                Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("允許準時提醒", fontWeight = FontWeight.Bold)
                    Text(
                        "Android 12 以上需要開啟「鬧鐘與提醒」權限，才能在預定時間精準通知。",
                        style = MaterialTheme.typography.bodySmall
                    )
                    TextButton(onClick = {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                "package:${context.packageName}".toUri()
                            )
                        )
                    }) { Text("前往開啟") }
                }
            }
        }

        Text("快速時間", Modifier.padding(horizontal = 20.dp, vertical = 12.dp), fontWeight = FontWeight.Bold)
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(30, 40, 45, 60).chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { minutes ->
                        FilledTonalButton(
                            onClick = { viewModel.startLaundry(minutes) },
                            modifier = Modifier.weight(1f)
                        ) { Text("$minutes 分鐘") }
                    }
                }
            }
            Button(onClick = { customDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Text("自訂時間")
            }
        }
        Spacer(Modifier.height(24.dp))
    }

    if (customDialog) {
        var minutes by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { customDialog = false },
            title = { Text("自訂洗衣時間") },
            text = {
                OutlinedTextField(
                    value = minutes,
                    onValueChange = { minutes = it.filter(Char::isDigit) },
                    label = { Text("分鐘") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                TextButton(
                    enabled = (minutes.toIntOrNull() ?: 0) > 0,
                    onClick = {
                        viewModel.startLaundry(minutes.toIntOrNull() ?: 1)
                        customDialog = false
                    }
                ) { Text("開始") }
            },
            dismissButton = { TextButton(onClick = { customDialog = false }) { Text("取消") } }
        )
    }
}
