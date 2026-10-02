package com.dormkit.app.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dormkit.app.model.Category
import com.dormkit.app.model.Item
import com.dormkit.app.model.LaundryTimer
import com.dormkit.app.model.PackingItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Category::class, Item::class, PackingItem::class, LaundryTimer::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class DormKitDatabase : RoomDatabase() {
    abstract fun dao(): DormKitDao

    companion object {
        @Volatile private var instance: DormKitDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE items ADD COLUMN location TEXT NOT NULL DEFAULT 'DORM'")
            }
        }

        fun getInstance(context: Context): DormKitDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                DormKitDatabase::class.java,
                "dormkit.db"
            ).addMigrations(MIGRATION_1_2).addCallback(object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    CoroutineScope(Dispatchers.IO).launch {
                        instance?.dao()?.insertCategories(
                            listOf(
                                Category(name = "衣物", icon = "👕"),
                                Category(name = "日用品", icon = "🧴"),
                                Category(name = "食品", icon = "🍜"),
                                Category(name = "3C", icon = "🔌"),
                                Category(name = "其他", icon = "📦")
                            )
                        )
                    }
                }
            }).build().also { instance = it }
        }
    }
}
