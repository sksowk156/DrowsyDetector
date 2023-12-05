package com.paradise.database.di

import android.content.Context
import com.paradise.database.provider.AnalyzeResultDataProvider
import com.paradise.database.provider.MusicDataProvider
import com.paradise.database.room.LocalDatabase
import com.paradise.database.room.dao.AnalyzeResultDao
import com.paradise.database.room.dao.MusicDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object LocalModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): LocalDatabase = LocalDatabase.getDatabase(context)


    @Provides
    @Singleton
    fun provideMusicDao(database: LocalDatabase): MusicDao = database.musicDao()

    @Provides
    @Singleton
    fun provideAnalyzeDao(database: LocalDatabase): AnalyzeResultDao = database.analyzeResultDao()

    @Provides
    @Singleton
    fun provideMusicData(musicDao: MusicDao): MusicDataProvider {
        return MusicDataProvider(musicDao)
    }

    @Provides
    @Singleton
    fun provideAnalyzeResultData(recorDao: AnalyzeResultDao): AnalyzeResultDataProvider {
        return AnalyzeResultDataProvider(recorDao)
    }
}