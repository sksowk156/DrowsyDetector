package com.paradise.drowsydetector.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import java.net.SocketTimeoutException

class RetrofitInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return try {
            chain.proceed(chain.request())
        } catch (e: SocketTimeoutException) {
            throw RuntimeException("요청 시간이 초과되었습니다.", e)
        }
    }
}