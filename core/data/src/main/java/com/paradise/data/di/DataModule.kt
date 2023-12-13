package com.paradise.data.di

import com.paradise.data.repository.AnalyzerResultRepository
import com.paradise.data.repository.MusicRepository
import com.paradise.data.repository.ParkingLotRepository
import com.paradise.data.repository.RestRepository
import com.paradise.data.repository.SettingRepository
import com.paradise.data.repository.ShelterRepository
import com.paradise.data.repositoryImpl.local.AnalyzerResultRepositoryImpl
import com.paradise.data.repositoryImpl.local.MusicRepositoryImpl
import com.paradise.data.repositoryImpl.local.SettingRepositoryImpl
import com.paradise.data.repositoryImpl.remote.ParkingLotRepositoryImpl
import com.paradise.data.repositoryImpl.remote.RestRepositoryImpl
import com.paradise.data.repositoryImpl.remote.ShelterRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
interface DataModule {
    @Binds
    fun provideParkingLotRepository(parkingLotDataProvider: ParkingLotRepositoryImpl): ParkingLotRepository

    @Binds
    fun provideShelterRepository(shelterDataProvider: ShelterRepositoryImpl): ShelterRepository

    @Binds
    fun provideRestRepository(restDataProvider: RestRepositoryImpl): RestRepository

    @Binds
    fun provideMusicRepository(musicDataProvider: MusicRepositoryImpl): MusicRepository

    @Binds
    fun provideStaticsRepository(analyzeResultDataProvider: AnalyzerResultRepositoryImpl): AnalyzerResultRepository

    @Binds
    fun provideSettingRepository(dataStoreProvider: SettingRepositoryImpl): SettingRepository
}