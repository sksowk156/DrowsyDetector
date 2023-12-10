//package com.paradise.drowsydetector.di
//
//import android.content.Context
//import androidx.datastore.core.DataStore
//import androidx.datastore.preferences.core.Preferences
//import androidx.datastore.preferences.preferencesDataStore
//import com.paradise.drowsydetector.data.local.room.LocalDatabase
//import com.paradise.drowsydetector.data.local.room.music.MusicDao
//import com.paradise.drowsydetector.data.local.room.record.AnalyzeResultDao
//import com.paradise.drowsydetector.data.remote.parkinglot.ParkingLotInterface
//import com.paradise.drowsydetector.data.remote.rest.RestInterface
//import com.paradise.drowsydetector.data.remote.shelter.ShelterInterface
//import com.paradise.drowsydetector.data.repository.MusicRepositoryImpl
//import com.paradise.drowsydetector.data.repository.RelaxRepositoryImpl
//import com.paradise.drowsydetector.data.repository.SettingRepositoryImpl
//import com.paradise.drowsydetector.data.repository.StaticsRepositoryImpl
//import com.paradise.drowsydetector.domain.repository.MusicRepository
//import com.paradise.drowsydetector.domain.repository.RelaxRepository
//import com.paradise.drowsydetector.domain.repository.SettingRepository
//import com.paradise.drowsydetector.domain.repository.StaticsRepository
//import com.paradise.drowsydetector.utils.BASE_URL
//import com.paradise.drowsydetector.utils.PREFERENCES_NAME
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.android.qualifiers.ApplicationContext
//import dagger.hilt.components.SingletonComponent
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import javax.inject.Singleton
//
//@Module
//@InstallIn(SingletonComponent::class)
//object AppModule {
//    @Provides
//    @Singleton
//    fun provideShelterInterface(): ShelterInterface =
//        Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create())
//            .build().create(ShelterInterface::class.java)
//
//    @Provides
//    @Singleton
//    fun provideRestInterface(): RestInterface =
//        Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create())
//            .build().create(RestInterface::class.java)
//
//    @Provides
//    @Singleton
//    fun provideParkingLotInterface(): ParkingLotInterface =
//        Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create())
//            .build().create(ParkingLotInterface::class.java)
//
//    @Provides
//    @Singleton
//    fun provideRelaxRepository(
//        shelterApi: ShelterInterface,
//        restApi: RestInterface,
//        parkingLotApi: ParkingLotInterface,
//    ): RelaxRepository =
//        RelaxRepositoryImpl(
//            shelterInterface = shelterApi,
//            restInterface = restApi,
//            parkingLotInterface = parkingLotApi
//        )
//
//
//    @Provides
//    @Singleton
//    fun provideDatabase(
//        @ApplicationContext context: Context,
//    ): LocalDatabase = LocalDatabase.getDatabase(context)
//
//
//    @Provides
//    @Singleton
//    fun provideMusicDao(database: LocalDatabase): MusicDao = database.musicDao()
//
//    @Provides
//    @Singleton
//    fun provideMusicRepository(musicDao: MusicDao): MusicRepository =
//        MusicRepositoryImpl(
//            musicDao = musicDao
//        )
//
//    @Provides
//    @Singleton
//    fun provideAnalyzeDao(database: LocalDatabase): AnalyzeResultDao = database.recordDao()
//
//    @Provides
//    @Singleton
//    fun provideStaticsRepository(analyzeResultDao: AnalyzeResultDao): StaticsRepository =
//        StaticsRepositoryImpl(
//            analyzeResultDao = analyzeResultDao
//        )
//
////    private val Context.settingDataStore by preferencesDataStore(PREFERENCES_NAME)
//
////    @Provides
////    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
////        context.settingDataStore
////
//    @Provides
//    fun provideSettingRepository(dataStore: DataStore<Preferences>): SettingRepository =
//        SettingRepositoryImpl(
//            dataStore = dataStore
//        )
//}