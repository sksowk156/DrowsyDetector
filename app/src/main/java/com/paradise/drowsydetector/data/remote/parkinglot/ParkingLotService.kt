package com.paradise.drowsydetector.data.remote.parkinglot

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ParkingLotService {
    companion object {
        private const val BASE_URL = "http://api.data.go.kr/openapi/"
        private lateinit var retrofitService: ParkingLotInterface
        fun getRetrofitRESTInstance(): ParkingLotInterface {
            if (!this::retrofitService.isInitialized) {
                val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                retrofitService = retrofit.create(ParkingLotInterface::class.java)
            }
            return retrofitService
        }
    }
}