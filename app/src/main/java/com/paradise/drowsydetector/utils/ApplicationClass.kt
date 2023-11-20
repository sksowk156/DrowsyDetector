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
import com.paradise.drowsydetector.repository.MusicRepository
import com.paradise.drowsydetector.repository.RelaxRepository
import com.paradise.drowsydetector.repository.SettingRepository
import com.paradise.drowsydetector.repository.StaticsRepository
import java.util.Locale

class ApplicationClass : Application() {
    companion object {
        private lateinit var appInstance: ApplicationClass
        fun getApplicationContext(): ApplicationClass = appInstance
    }

    private val dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCES_NAME)

    private val database by lazy { LocalDatabase.getInstance(this) }

    val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(
            this
        )
    }

    val geocoder by lazy { Geocoder(this, Locale.KOREA) }

    val musicRepository by lazy {
        MusicRepository.getInstance(
            musicDao = database.musicDao()
        )
    }

    val relaxRepository by lazy {
        RelaxRepository.getInstance(
            drowyShelterInterface = DrowsyShelterService.getRetrofitRESTInstance(),
            parkingLotInterface = ParkingLotService.getRetrofitRESTInstance(),
            restInterface = RestService.getRetrofitRESTInstance()
        )
    }

    val settingRepository by lazy {
        SettingRepository.getInstance(
            dataStore = dataStore
        )
    }


    val staticRepository by lazy {
        StaticsRepository.getInstance(
            recordDao = database.recordDao(),
            winkCountDao = database.winkCountDao(),
            drowsyCountDao = database.drowsyCountDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        appInstance = this
    }
}