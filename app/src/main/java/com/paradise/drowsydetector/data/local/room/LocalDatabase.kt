package com.paradise.drowsydetector.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.paradise.drowsydetector.data.local.room.music.Music
import com.paradise.drowsydetector.data.local.room.music.MusicDao

@Database(entities = [Music::class], version = 1, exportSchema = false)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao

    companion object {
        @Volatile
        private var instance: LocalDatabase? = null

        fun getInstance(context: Context): LocalDatabase = instance ?: synchronized(this) {
            instance ?: buildDatabase(context).also { instance = it }
        }

        private fun buildDatabase(context: Context): LocalDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                LocalDatabase::class.java, "local_database.db"
            ).build()
    }
}