package com.dormkit.app.repository

import com.dormkit.app.database.DormKitDao
import com.dormkit.app.model.Category
import com.dormkit.app.model.Item
import com.dormkit.app.model.LaundryTimer
import com.dormkit.app.model.PackingItem

class DormRepository(private val dao: DormKitDao) {
    val categories = dao.observeCategories()
    val items = dao.observeItems()
    val packingItems = dao.observePackingItems()
    val laundryTimer = dao.observeLaundryTimer()

    suspend fun saveCategory(category: Category) = dao.upsertCategory(category)
    suspend fun deleteCategory(category: Category) = dao.deleteCategory(category)
    suspend fun saveItem(item: Item) = dao.upsertItem(item)
    suspend fun deleteItem(item: Item) = dao.deleteItem(item)
    suspend fun adjustItemQuantity(id: Long, delta: Int) = dao.adjustItemQuantity(id, delta)
    suspend fun savePackingItem(item: PackingItem) = dao.upsertPackingItem(item)
    suspend fun deletePackingItem(item: PackingItem) = dao.deletePackingItem(item)
    suspend fun setPackingChecked(id: Long, checked: Boolean) = dao.setPackingChecked(id, checked)
    suspend fun adjustPackingQuantity(id: Long, delta: Int) = dao.adjustPackingQuantity(id, delta)
    suspend fun saveLaundryTimer(timer: LaundryTimer) = dao.upsertLaundryTimer(timer)
    suspend fun deactivateLaundryTimer() = dao.deactivateLaundryTimer()
}
