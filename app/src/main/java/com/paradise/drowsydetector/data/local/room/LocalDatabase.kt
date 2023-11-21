package com.paradise.drowsydetector.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.paradise.drowsydetector.data.local.room.music.Music
import com.paradise.drowsydetector.data.local.room.music.MusicDao
import com.paradise.drowsydetector.data.local.room.record.DrowsyCount
import com.paradise.drowsydetector.data.local.room.record.DrowsyCountDao
import com.paradise.drowsydetector.data.local.room.record.DrowsyRecord
import com.paradise.drowsydetector.data.local.room.record.RecordDao
import com.paradise.drowsydetector.data.local.room.record.WinkCount
import com.paradise.drowsydetector.data.local.room.record.WinkCountDao

@Database(
    entities = [Music::class, DrowsyRecord::class, WinkCount::class, DrowsyCount::class],
    version = 1,
    exportSchema = false
)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao
    abstract fun recordDao(): RecordDao
    abstract fun winkCountDao(): WinkCountDao
    abstract fun drowsyCountDao(): DrowsyCountDao

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