package com.paradise.data.di

import com.paradise.data.repository.AnalyzerResultRepository
import com.paradise.data.repository.MusicRepository
import com.paradise.data.repository.ParkingLotRepository
import com.paradise.data.repository.RestRepository
import com.paradise.data.repository.SettingRepository
import com.paradise.data.repository.ShelterRepository
import com.paradise.data.repositoryImpl.AnalyzerResultRepositoryImpl
import com.paradise.data.repositoryImpl.MusicRepositoryImpl
import com.paradise.data.repositoryImpl.ParkingLotRepositoryImpl
import com.paradise.data.repositoryImpl.RestRepositoryImpl
import com.paradise.data.repositoryImpl.SettingRepositoryImpl
import com.paradise.data.repositoryImpl.ShelterRepositoryImpl
import com.paradise.database.provider.AnalyzeResultDataProvider
import com.paradise.database.provider.MusicDataProvider
import com.paradise.datastore.provider.DataStoreProvider
import com.paradise.network.provider.ParkingLotDataProvider
import com.paradise.network.provider.RestDataProvider
import com.paradise.network.provider.ShelterDataProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DataModule {
    @Provides
    @Singleton
    fun provideParkingLotRepository(parkingLotDataProvider: ParkingLotDataProvider): ParkingLotRepository {
        return ParkingLotRepositoryImpl(parkingLotDataProvider)
    }

    @Provides
    @Singleton
    fun provideShelterRepository(shelterDataProvider: ShelterDataProvider): ShelterRepository {
        return ShelterRepositoryImpl(shelterDataProvider)
    }

    @Provides
    @Singleton
    fun provideRestRepository(restDataProvider: RestDataProvider): RestRepository {
        return RestRepositoryImpl(restDataProvider)
    }

    @Provides
    @Singleton
    fun provideMusicRepository(musicDataProvider: MusicDataProvider): MusicRepository =
        MusicRepositoryImpl(
            musicDataProvider = musicDataProvider
        )

    @Provides
    @Singleton
    fun provideStaticsRepository(analyzeResultDataProvider: AnalyzeResultDataProvider): AnalyzerResultRepository =
        AnalyzerResultRepositoryImpl(
            analyzeResultDataProvider = analyzeResultDataProvider
        )

    @Provides
    @Singleton
    fun provideSettingRepository(dataStoreProvider: DataStoreProvider): SettingRepository =
        SettingRepositoryImpl(
            dataStoreProvider = dataStoreProvider
        )
}