package com.dormkit.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dormkit.app.model.StorageLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

class SettingsStore(private val context: Context) {
    private val darkModeKey = booleanPreferencesKey("dark_mode")
    private val storageLocationKey = stringPreferencesKey("storage_location")

    val darkMode: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[darkModeKey] ?: false
    }

    val storageLocation: Flow<StorageLocation> = context.settingsDataStore.data.map { preferences ->
        preferences[storageLocationKey]
            ?.let { runCatching { StorageLocation.valueOf(it) }.getOrNull() }
            ?: StorageLocation.DORM
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.settingsDataStore.edit { it[darkModeKey] = enabled }
    }

    suspend fun setStorageLocation(location: StorageLocation) {
        context.settingsDataStore.edit { it[storageLocationKey] = location.name }
    }
}
