package com.paradise.drowsydetector.utils

import android.app.Application
import android.location.Geocoder
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.gms.location.LocationServices
import com.paradise.drowsydetector.data.local.room.LocalDatabase
import com.paradise.drowsydetector.data.remote.parkinglot.ParkingLotService
import com.paradise.drowsydetector.data.remote.rest.RestService
import com.paradise.drowsydetector.data.remote.shelter.DrowsyShelterService
import com.paradise.drowsydetector.data.repository.MusicRepositoryImpl
import com.paradise.drowsydetector.data.repository.RelaxRepositoryImpl
import com.paradise.drowsydetector.data.repository.SettingRepositoryImpl
import com.paradise.drowsydetector.data.repository.StaticsRepositoryImpl
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale

@HiltAndroidApp
class ApplicationClass : Application() {
    companion object {
        private lateinit var appInstance: ApplicationClass
        fun getApplicationContext(): ApplicationClass = appInstance
    }

    private val dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCES_NAME)

    private val database by lazy { LocalDatabase.getDatabase(this) }

    val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(
            this
        )
    }

    val geocoder by lazy { Geocoder(this, Locale.KOREA) }

    val musicRepositoryImpl by lazy {
        MusicRepositoryImpl.getInstance(
            musicDao = database.musicDao()
        )
    }

    val relaxRepositoryImpl by lazy {
        RelaxRepositoryImpl.getInstance(
            shelterInterface = DrowsyShelterService.getRetrofitRESTInstance(),
            parkingLotInterface = ParkingLotService.getRetrofitRESTInstance(),
            restInterface = RestService.getRetrofitRESTInstance()
        )
    }

    val settingRepositoryImpl by lazy {
        SettingRepositoryImpl.getInstance(
            dataStore = dataStore
        )
    }

    val staticRepository by lazy {
        StaticsRepositoryImpl.getInstance(
            analyzeResultDao = database.recordDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        appInstance = this
    }
}