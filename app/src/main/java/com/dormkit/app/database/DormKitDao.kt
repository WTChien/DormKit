package com.dormkit.app.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dormkit.app.model.Category
import com.dormkit.app.model.Item
import com.dormkit.app.model.ItemWithCategory
import com.dormkit.app.model.LaundryTimer
import com.dormkit.app.model.PackingItem
import kotlinx.coroutines.flow.Flow

@Dao
interface DormKitDao {
    @Query("SELECT * FROM categories ORDER BY id")
    fun observeCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategory(category: Category): Long

    @Delete
    suspend fun deleteCategory(category: Category)

    @Insert
    suspend fun insertCategories(categories: List<Category>)

    @Query(
        """
        SELECT items.*, categories.name AS category_name, categories.icon AS category_icon
        FROM items LEFT JOIN categories ON items.category_id = categories.id
        ORDER BY items.name COLLATE NOCASE
        """
    )
    fun observeItems(): Flow<List<ItemWithCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItem(item: Item): Long

    @Delete
    suspend fun deleteItem(item: Item)

    @Query("UPDATE items SET quantity = MAX(0, quantity + :delta) WHERE id = :id")
    suspend fun adjustItemQuantity(id: Long, delta: Int)

    @Query("SELECT * FROM packing_items ORDER BY checked ASC, id DESC")
    fun observePackingItems(): Flow<List<PackingItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPackingItem(item: PackingItem): Long

    @Delete
    suspend fun deletePackingItem(item: PackingItem)

    @Query("UPDATE packing_items SET checked = :checked WHERE id = :id")
    suspend fun setPackingChecked(id: Long, checked: Boolean)

    @Query("UPDATE packing_items SET quantity = MAX(1, quantity + :delta) WHERE id = :id")
    suspend fun adjustPackingQuantity(id: Long, delta: Int)

    @Query("SELECT * FROM laundry_timers WHERE id = 1 LIMIT 1")
    fun observeLaundryTimer(): Flow<LaundryTimer?>

    @Query("SELECT * FROM laundry_timers WHERE id = 1 LIMIT 1")
    suspend fun getLaundryTimer(): LaundryTimer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLaundryTimer(timer: LaundryTimer)

    @Query("UPDATE laundry_timers SET active = 0 WHERE id = 1")
    suspend fun deactivateLaundryTimer()
}
