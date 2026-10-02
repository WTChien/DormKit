package com.dormkit.app

import android.app.Application
import com.dormkit.app.data.SettingsStore
import com.dormkit.app.database.DormKitDatabase
import com.dormkit.app.notification.LaundryNotifier
import com.dormkit.app.repository.DormRepository

class DormKitApplication : Application() {
    val database by lazy { DormKitDatabase.getInstance(this) }
    val repository by lazy { DormRepository(database.dao()) }
    val settingsStore by lazy { SettingsStore(this) }

    override fun onCreate() {
        super.onCreate()
        LaundryNotifier.createChannel(this)
    }
}
