//package com.paradise.drowsydetector.data.remote.parkinglot
//
//import com.paradise.drowsydetector.data.remote.RetrofitInterceptor
//import okhttp3.OkHttpClient
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import java.util.concurrent.TimeUnit
//
//class ParkingLotService {
//    companion object {
//        private const val BASE_URL = "http://api.data.go.kr/openapi/"
//        private lateinit var retrofitService: ParkingLotInterface
//        fun getRetrofitRESTInstance(): ParkingLotInterface {
//            if (!this::retrofitService.isInitialized) {
//                val retrofit = Retrofit.Builder()
//                    .baseUrl(BASE_URL)
//                    .client(provideOkHttpClientBuilder())
//                    .addConverterFactory(GsonConverterFactory.create())
//                    .build()
//                retrofitService = retrofit.create(ParkingLotInterface::class.java)
//            }
//            return retrofitService
//        }
//
//        fun provideOkHttpClientBuilder(): OkHttpClient {
//            val connectTimeout: Long = 30 * 1000
//            val readTimeout: Long = 15 * 1000
//            return OkHttpClient.Builder()
//                .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
//                .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
////                .addInterceptor(RetrofitInterceptor())
//                .build()
//        }
//    }
//}