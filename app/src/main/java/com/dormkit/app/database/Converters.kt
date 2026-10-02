package com.dormkit.app.database

import androidx.room.TypeConverter
import com.dormkit.app.model.PackingDirection
import com.dormkit.app.model.StorageLocation

class Converters {
    @TypeConverter
    fun fromDirection(value: PackingDirection): String = value.name

    @TypeConverter
    fun toDirection(value: String): PackingDirection = PackingDirection.valueOf(value)

    @TypeConverter
    fun fromStorageLocation(value: StorageLocation): String = value.name

    @TypeConverter
    fun toStorageLocation(value: String): StorageLocation = StorageLocation.valueOf(value)
}
