package com.paradise.data.di

import com.paradise.data.repositoryImpl.ParkingLotRepositoryImpl
import com.paradise.data.repositoryImpl.RestRepositoryImpl
import com.paradise.data.repositoryImpl.ShelterRepositoryImpl
import com.paradise.domain.repository.ParkingLotRepository
import com.paradise.domain.repository.RestRepository
import com.paradise.domain.repository.ShelterRepository
import com.paradise.network.provider.ParkingLotDataProvider
import com.paradise.network.provider.RestDataProvider
import com.paradise.network.provider.ShelterDataProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object DataModule {
    @Provides
    fun provideParkingLotRepository(parkingLotDataProvider: ParkingLotDataProvider): ParkingLotRepository {
        return ParkingLotRepositoryImpl(parkingLotDataProvider)
    }

    @Provides
    fun provideShelterRepository(shelterDataProvider: ShelterDataProvider): ShelterRepository {
        return ShelterRepositoryImpl(shelterDataProvider)
    }

    @Provides
    fun provideRestRepository(restDataProvider: RestDataProvider): RestRepository {
        return RestRepositoryImpl(restDataProvider)
    }
}