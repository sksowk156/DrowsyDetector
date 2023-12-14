package com.paradise.drowsydetector.multimodule

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ApplicationClass : Application() {
    override fun onCreate() {
        super.onCreate()
//        appInstance = this
    }
//    companion object {
//        private lateinit var appInstance: ApplicationClass
//        fun getApplicationContext(): ApplicationClass = appInstance
//    }

//    private val dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCES_NAME)
//
//    private val database by lazy { LocalDatabase.getDatabase(this) }
//
//    val fusedLocationProviderClient by lazy {
//        LocationServices.getFusedLocationProviderClient(
//            this
//        )
//    }
//
//    val geocoder by lazy { Geocoder(this, Locale.KOREA) }
//
//    val musicRepositoryImpl by lazy {
//        MusicRepositoryImpl.getInstance(
//            musicDao = database.musicDao()
//        )
//    }
//
//    val relaxRepositoryImpl by lazy {
//        RelaxRepositoryImpl.getInstance(
//            shelterInterface = DrowsyShelterService.getRetrofitRESTInstance(),
//            parkingLotInterface = ParkingLotService.getRetrofitRESTInstance(),
//            restInterface = RestService.getRetrofitRESTInstance()
//        )
//    }
//
//    val settingRepositoryImpl by lazy {
//        SettingRepositoryImpl.getInstance(
//            dataStore = dataStore
//        )
//    }
//
//    val staticRepository by lazy {
//        StaticsRepositoryImpl.getInstance(
//            analyzeResultDao = database.recordDao()
//        )
//    }
}