package com.paradise.drowsydetector.data.remote.rest

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RestService {
    companion object {
        private const val BASE_URL = "http://api.data.go.kr/openapi/"
        private lateinit var restService: RestInterface
        fun getRetrofitRESTInstance(): RestInterface {
            if (!this::restService.isInitialized) {
                val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                restService = retrofit.create(RestInterface::class.java)
            }
            return restService
        }
    }
}