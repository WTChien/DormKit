package com.dormkit.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dormkit.app.data.SettingsStore
import com.dormkit.app.model.Category
import com.dormkit.app.model.Item
import com.dormkit.app.model.ItemWithCategory
import com.dormkit.app.model.LaundryTimer
import com.dormkit.app.model.PackingItem
import com.dormkit.app.model.StorageLocation
import com.dormkit.app.notification.LaundryScheduler
import com.dormkit.app.repository.DormRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class InventoryFilter(
    val search: String = "",
    val categoryId: Long? = null,
    val lowOnly: Boolean = false
)

class MainViewModel(
    application: Application,
    private val repository: DormRepository,
    private val settingsStore: SettingsStore
) : AndroidViewModel(application) {
    val categories = repository.categories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val allItems = repository.items.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val packingItems = repository.packingItems.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val laundryTimer = repository.laundryTimer.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val darkMode = settingsStore.darkMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
    val storageLocation = settingsStore.storageLocation.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        StorageLocation.DORM
    )

    val locationItems: StateFlow<List<ItemWithCategory>> = combine(allItems, storageLocation) { items, location ->
        items.filter { it.location == location }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val inventoryFilter = MutableStateFlow(InventoryFilter())
    val visibleItems: StateFlow<List<ItemWithCategory>> = combine(locationItems, inventoryFilter) { items, filter ->
        items.filter { item ->
            (filter.search.isBlank() || item.name.contains(filter.search, ignoreCase = true)) &&
                (filter.categoryId == null || item.categoryId == filter.categoryId) &&
                (!filter.lowOnly || item.isLowStock)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val filter: StateFlow<InventoryFilter> = inventoryFilter

    fun setSearch(value: String) { inventoryFilter.value = inventoryFilter.value.copy(search = value) }
    fun setCategoryFilter(id: Long?) { inventoryFilter.value = inventoryFilter.value.copy(categoryId = id) }
    fun showLowStockOnly(enabled: Boolean) { inventoryFilter.value = inventoryFilter.value.copy(lowOnly = enabled) }
    fun clearInventoryFilters() { inventoryFilter.value = InventoryFilter() }

    fun saveItem(item: Item) = launch { repository.saveItem(item) }
    fun deleteItem(item: Item) = launch { repository.deleteItem(item) }
    fun adjustItem(id: Long, delta: Int) = launch { repository.adjustItemQuantity(id, delta) }
    fun saveCategory(category: Category) = launch { repository.saveCategory(category) }
    fun deleteCategory(category: Category) = launch { repository.deleteCategory(category) }
    fun savePackingItem(item: PackingItem) = launch { repository.savePackingItem(item) }
    fun deletePackingItem(item: PackingItem) = launch { repository.deletePackingItem(item) }
    fun setPackingChecked(id: Long, checked: Boolean) = launch { repository.setPackingChecked(id, checked) }
    fun adjustPacking(id: Long, delta: Int) = launch { repository.adjustPackingQuantity(id, delta) }
    fun setDarkMode(enabled: Boolean) = launch { settingsStore.setDarkMode(enabled) }
    fun setStorageLocation(location: StorageLocation) = launch { settingsStore.setStorageLocation(location) }

    fun startLaundry(minutes: Int) {
        val now = System.currentTimeMillis()
        val end = now + minutes.coerceAtLeast(1) * 60_000L
        launch {
            repository.saveLaundryTimer(LaundryTimer(startTime = now, endTime = end, active = true))
            LaundryScheduler.schedule(getApplication(), end)
        }
    }

    fun cancelLaundry() {
        launch {
            repository.deactivateLaundryTimer()
            LaundryScheduler.cancel(getApplication())
        }
    }

    private fun launch(block: suspend () -> Unit) = viewModelScope.launch { block() }

    class Factory(
        private val application: Application,
        private val repository: DormRepository,
        private val settingsStore: SettingsStore
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MainViewModel(application, repository, settingsStore) as T
    }
}
