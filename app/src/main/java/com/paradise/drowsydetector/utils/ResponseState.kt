package com.paradise.drowsydetector.utils

sealed class ResponseState<out T> { // out 키워드가 있으면 ResponseStatae<T의 자식클래스> 또한 ResponseState<T>의 자식클래스로 인식한다. -> Covariant
    object Loading : ResponseState<Nothing>() // 어떤 데이터를 호출할 때도 값이 없는 것이기 때문에 싱글톤으로 구현
    data class Success<out T>(val data: T) : ResponseState<T>()
    data class Fail<out T>(val code: Int, val message: String?) : ResponseState<T>() // 데이터 전송 실패시
    data class Error<out T>(val exception: Throwable) : ResponseState<T>() // 예외 발생시

}

//sealed class ResponseState<T>(
//    val data: T? = null,
//    val message: String? = null,
//) {
//    class Success<T>(data: T) : ResponseState<T>(data)
//    class Error<T>(message: String, data: T? = null) : ResponseState<T>(data, message)
//    class Loading<T> : ResponseState<T>()
//}