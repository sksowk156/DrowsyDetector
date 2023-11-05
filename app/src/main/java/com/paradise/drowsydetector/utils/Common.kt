package com.paradise.drowsydetector.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.paradise.drowsydetector.utils.ApplicationClass.Companion.getApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

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