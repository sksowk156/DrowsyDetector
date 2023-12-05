package com.paradise.domain.di

import com.paradise.domain.repository.AnalyzerResultRepository
import com.paradise.domain.repository.MusicRepository
import com.paradise.domain.repository.ParkingLotRepository
import com.paradise.domain.repository.RestRepository
import com.paradise.domain.repository.ShelterRepository
import com.paradise.domain.usecases.GetAnalyzeResultItemListUseCase
import com.paradise.domain.usecases.GetMusicItemListUseCase
import com.paradise.domain.usecases.GetParkingLostItemListUseCase
import com.paradise.domain.usecases.GetRestItemListUseCase
import com.paradise.domain.usecases.GetShelterItemListUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object DomainModule {
    @Provides
    fun provideGetParkingLotItemListUseCase(parkingLotRepository: ParkingLotRepository): GetParkingLostItemListUseCase {
        return GetParkingLostItemListUseCase(parkingLotRepository)
    }

    @Provides
    fun provideGetShelterItemListUseCase(shelterRepository: ShelterRepository): GetShelterItemListUseCase {
        return GetShelterItemListUseCase(shelterRepository)
    }

    @Provides
    fun provideGetRestItemListUseCase(restRepository: RestRepository): GetRestItemListUseCase {
        return GetRestItemListUseCase(restRepository)
    }

    @Provides
    fun provideGetMusicItemListUseCase(musicRepository: MusicRepository): GetMusicItemListUseCase {
        return GetMusicItemListUseCase(musicRepository)
    }

    @Provides
    fun provideGetAnalyzeResultItemListUseCase(analyzerResultRepository: AnalyzerResultRepository): GetAnalyzeResultItemListUseCase {
        return GetAnalyzeResultItemListUseCase(analyzerResultRepository)
    }
}