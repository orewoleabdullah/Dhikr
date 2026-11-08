package com.abdullah.dhikr.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.abdullah.dhikr.data.local.dao.DhikrDao
import com.abdullah.dhikr.data.local.dao.DhikrPresetDao
import com.abdullah.dhikr.data.local.dao.DhikrSessionDao
import com.abdullah.dhikr.data.local.entity.DhikrPresetEntity
import com.abdullah.dhikr.data.local.entity.DhikrSessionEntity
import com.abdullah.dhikr.domain.model.DhikrItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [DhikrItem::class, DhikrPresetEntity::class, DhikrSessionEntity::class],
    version = 3,
    exportSchema = false
)
abstract class DhikrDatabase : RoomDatabase() {

    abstract fun dhikrDao(): DhikrDao
    abstract fun dhikrPresetDao(): DhikrPresetDao
    abstract fun dhikrSessionDao(): DhikrSessionDao

    companion object {
        @Volatile
        private var Instance: DhikrDatabase? = null

        fun getDatabase(context: Context): DhikrDatabase {
            return Instance ?: synchronized(this) {
                val appContext = context.applicationContext
                Room.databaseBuilder(appContext, DhikrDatabase::class.java, "dhikr_database")
                    .addCallback(DhikrDatabaseCallback(appContext))
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }

        private class DhikrDatabaseCallback(
            private val appContext: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val database = getDatabase(appContext)
                        database.dhikrDao().insertAll(DhikrItem.BUILT_IN_PRESETS)
                        database.dhikrPresetDao().insertPresets(DhikrPresetEntity.BUILT_IN_PRESETS)
                    } catch (_: Exception) {}
                }
            }
        }
    }
}
