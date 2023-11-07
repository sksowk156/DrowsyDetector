package com.paradise.drowsydetector.utils

import android.location.Geocoder
import android.location.Location
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.paradise.drowsydetector.utils.ApplicationClass.Companion.getApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.Locale

// BaseFragment에서 사용하는 typealias
typealias FragmentInflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T


// Rx Event 에러 태그
const val RXERROR = "RX_ERROR"

// Rx Event 더블 클릭 간격 시간
const val CLICK_INTERVAL_TIME = 300L

// Rx Event 텍스트 완성 시간
const val INPUT_COMPLETE_TIME = 1000L

val defaultDispatcher = Dispatchers.Default
val ioDispatcher = Dispatchers.IO
val mainDispatcher = Dispatchers.Main

val defaultScope = CoroutineScope(defaultDispatcher)
val ioScope = CoroutineScope(ioDispatcher)
val mainScope = CoroutineScope(mainDispatcher)

val MAINBASE = "homebase"
val ANALYZE = "analye"
val HOME = "home"
val SETTING = "setting"
val STATISTIC = "statistic"
val CUURRENTFRAGMENTTAG = "currentfragment"
fun showToast(message: String) {
    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show()
}


//위도 경도로 주소 구하는 Reverse-GeoCoding
private fun getAddress(location: Location): String {
    return try {
        with(Geocoder(getApplicationContext(), Locale.KOREA).getFromLocation(location.latitude, location.longitude, 1)!!.first()){
            getAddressLine(0)   //주소
            countryName     //국가이름 (대한민국)
            countryCode     //국가코드
            adminArea       //행정구역 (서울특별시)
            locality        //관할구역 (중구)
            thoroughfare    //상세구역 (봉래동2가)
            featureName     //상세주소 (122-21)
        }
    } catch (e: Exception){
        e.printStackTrace()
        getAddress(location)
    }
}