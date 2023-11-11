package com.paradise.drowsydetector.utils

import android.app.Application
import com.paradise.drowsydetector.data.local.music.LocalDatabase
import com.paradise.drowsydetector.data.remote.parkinglot.ParkingLotService
import com.paradise.drowsydetector.data.remote.shelter.DrowsyShelterService
import com.paradise.drowsydetector.repository.MusicRepository
import com.paradise.drowsydetector.repository.RelaxRepository

class ApplicationClass : Application() {
    companion object {
        private lateinit var appInstance: ApplicationClass
        fun getApplicationContext(): ApplicationClass = appInstance
    }

    val database by lazy { LocalDatabase.getInstance(this) }

    val musicRepository by lazy {
        MusicRepository.getInstance(
            musicDao = database.musicDao()
        )
    }

    val relaxRepository by lazy {
        RelaxRepository.getInstance(
            drowyShelterInterface = DrowsyShelterService.getRetrofitRESTInstance(),
            parkingLotInterface = ParkingLotService.getRetrofitRESTInstance()
        )
    }

    override fun onCreate() {
        super.onCreate()
        appInstance = this
    }
}