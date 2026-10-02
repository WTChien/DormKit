package com.dormkit.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dormkit.app.navigation.DormKitApp
import com.dormkit.app.ui.theme.DormKitTheme
import com.dormkit.app.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as DormKitApplication
        setContent {
            val viewModel: MainViewModel = viewModel(
                factory = MainViewModel.Factory(app, app.repository, app.settingsStore)
            )
            val darkMode by viewModel.darkMode.collectAsStateWithLifecycle()
            val storageLocation by viewModel.storageLocation.collectAsStateWithLifecycle()
            val notificationPermission = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { }
            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= 33) {
                    notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            DormKitTheme(darkTheme = darkMode, storageLocation = storageLocation) {
                DormKitApp(viewModel)
            }
        }
    }
}
