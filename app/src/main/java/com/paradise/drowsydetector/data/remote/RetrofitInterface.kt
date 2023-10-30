package com.paradise.drowsydetector.data.remote

import retrofit2.Response
import retrofit2.http.GET

const val TARGET_ADDRESS = "\thttp://api.data.go.kr/openapi/"

interface RetrofitInterface {

//    @GET("tn_pubr_public_drowsy_shelter_api?serviceKey=")
//    suspend fun getAllMovies() : Response<List<MovieInfo>>

    /**
     * enqueue  비동기로 호출시 REST 함수
     */
    //suspend fun getAllMovies() : Call<List<MovieInfo>>
}