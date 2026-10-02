package com.dormkit.app.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String = "📦"
)

enum class StorageLocation(val label: String, val inventoryTitle: String) {
    DORM("宿舍", "宿舍庫存"),
    ROOM("家裡房間", "房間庫存");

    val other: StorageLocation
        get() = if (this == DORM) ROOM else DORM
}

@Entity(
    tableName = "items",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("category_id")]
)
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "category_id") val categoryId: Long?,
    val quantity: Int = 0,
    val unit: String = "個",
    @ColumnInfo(name = "minimum_quantity") val minimumQuantity: Int = 0,
    val note: String = "",
    @ColumnInfo(defaultValue = "'DORM'") val location: StorageLocation = StorageLocation.DORM
)

enum class PackingDirection(val label: String) {
    DORM_TO_HOME("帶回家的"),
    HOME_TO_DORM("帶回宿舍的")
}

@Entity(tableName = "packing_items")
data class PackingItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val direction: PackingDirection,
    val quantity: Int = 1,
    val unit: String = "個",
    val checked: Boolean = false,
    val note: String = ""
)

@Entity(tableName = "laundry_timers")
data class LaundryTimer(
    @PrimaryKey val id: Long = ACTIVE_TIMER_ID,
    @ColumnInfo(name = "start_time") val startTime: Long,
    @ColumnInfo(name = "end_time") val endTime: Long,
    val active: Boolean = true
) {
    companion object {
        const val ACTIVE_TIMER_ID = 1L
    }
}

data class ItemWithCategory(
    val id: Long,
    val name: String,
    @ColumnInfo(name = "category_id") val categoryId: Long?,
    val quantity: Int,
    val unit: String,
    @ColumnInfo(name = "minimum_quantity") val minimumQuantity: Int,
    val note: String,
    val location: StorageLocation,
    @ColumnInfo(name = "category_name") val categoryName: String?,
    @ColumnInfo(name = "category_icon") val categoryIcon: String?
) {
    val isLowStock: Boolean get() = quantity <= minimumQuantity
}
