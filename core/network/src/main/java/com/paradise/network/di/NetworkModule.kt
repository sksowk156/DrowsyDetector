package com.paradise.network.di

import com.paradise.common.network.BASE_URL
import com.paradise.network.provider.ParkingLotDataProvider
import com.paradise.network.provider.RestDataProvider
import com.paradise.network.provider.ShelterDataProvider
import com.paradise.network.retrofit.parkinglot.ParkingLotService
import com.paradise.network.retrofit.rest.RestService
import com.paradise.network.retrofit.shelter.ShelterService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {
    @Provides
    @Singleton
    fun provideShelterInterface(): ShelterService =
        Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create())
            .build().create(ShelterService::class.java)

    @Provides
    @Singleton
    fun provideRestInterface(): RestService =
        Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create())
            .build().create(RestService::class.java)

    @Provides
    @Singleton
    fun provideParkingLotInterface(): ParkingLotService =
        Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create())
            .build().create(ParkingLotService::class.java)

    @Provides
    @Singleton
    fun provideShelterData(shelterService: ShelterService): ShelterDataProvider {
        return ShelterDataProvider(shelterService)
    }

    @Provides
    @Singleton
    fun provideRestData(restService: RestService): RestDataProvider {
        return RestDataProvider(restService)
    }

    @Provides
    @Singleton
    fun provideParkingLotData(parkingLotService: ParkingLotService): ParkingLotDataProvider {
        return ParkingLotDataProvider(parkingLotService)
    }
}