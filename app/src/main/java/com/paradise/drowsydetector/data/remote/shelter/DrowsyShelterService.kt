package com.paradise.drowsydetector.data.remote.shelter

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DrowsyShelterService {
    companion object {
        private const val BASE_URL = "http://api.data.go.kr/openapi/"
        private lateinit var retrofitService: DrowyShelterInterface
        fun getRetrofitRESTInstance(): DrowyShelterInterface {
            if (!this::retrofitService.isInitialized) {
                val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                retrofitService = retrofit.create(DrowyShelterInterface::class.java)
            }
            return retrofitService
        }
    }
}