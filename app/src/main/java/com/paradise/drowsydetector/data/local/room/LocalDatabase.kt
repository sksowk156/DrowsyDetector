package com.paradise.drowsydetector.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.paradise.drowsydetector.data.local.room.music.Music
import com.paradise.drowsydetector.data.local.room.music.MusicDao
import com.paradise.drowsydetector.data.local.room.record.AnalyzeResult
import com.paradise.drowsydetector.data.local.room.record.DrowsyCount
import com.paradise.drowsydetector.data.local.room.record.AnalyzeResultDao
import com.paradise.drowsydetector.data.local.room.record.WinkCount

@Database(
    entities = [Music::class, AnalyzeResult::class, WinkCount::class, DrowsyCount::class],
    version = 1,
    exportSchema = false
)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao
    abstract fun recordDao(): AnalyzeResultDao

    companion object {
        @Volatile
        private var instance: LocalDatabase? = null

        fun getInstance(context: Context): LocalDatabase = instance ?: synchronized(this) {
            instance ?: buildDatabase(context).also { instance = it }
        }

        private fun buildDatabase(context: Context): LocalDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                LocalDatabase::class.java, "localdatabase.db"
            ).build()
    }
}