package com.paradise.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.paradise.database.room.dao.AnalyzeResultDao
import com.paradise.database.room.dao.MusicDao
import com.paradise.database.room.model.AnalyzeResult
import com.paradise.database.room.model.DrowsyCount
import com.paradise.database.room.model.Music
import com.paradise.database.room.model.WinkCount

@Database(
    entities = [Music::class, AnalyzeResult::class, WinkCount::class, DrowsyCount::class],
    version = 1,
    exportSchema = false
)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao
    abstract fun analyzeResultDao(): AnalyzeResultDao

    companion object {
        @Volatile
        private var database: LocalDatabase? = null

        fun getDatabase(context: Context): LocalDatabase {
            return database ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LocalDatabase::class.java,
                    "localdatabase.db"
                ).build()
                database = instance
                instance
            }
        }
    }
}