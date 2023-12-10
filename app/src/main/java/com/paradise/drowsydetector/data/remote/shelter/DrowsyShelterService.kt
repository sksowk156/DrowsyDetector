//package com.paradise.drowsydetector.data.remote.shelter
//
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//
//class DrowsyShelterService {
//    companion object {
//        private const val BASE_URL = "http://api.data.go.kr/openapi/"
//        private lateinit var retrofitService: ShelterInterface
//        fun getRetrofitRESTInstance(): ShelterInterface {
//            if (!this::retrofitService.isInitialized) {
//                val retrofit = Retrofit.Builder()
//                    .baseUrl(BASE_URL)
//                    .addConverterFactory(GsonConverterFactory.create())
//                    .build()
//                retrofitService = retrofit.create(ShelterInterface::class.java)
//            }
//            return retrofitService
//        }
//    }
//}