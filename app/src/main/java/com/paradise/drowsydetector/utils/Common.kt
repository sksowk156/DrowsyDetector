package com.paradise.drowsydetector.utils

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import com.paradise.drowsydetector.utils.ApplicationClass.Companion.getApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.File
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

//Uri -> Path(파일경로)
fun getPathFromFileUri(context: Context, contentUri: Uri?): String? {
    val projection = arrayOf(MediaStore.Audio.Media.DATA)
    context.contentResolver.query(contentUri!!, projection, null, null, null)?.use { cursor ->
        val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
        if (cursor.moveToFirst()) {
            return cursor.getString(columnIndex)
        }
    }
    return null
}

// Path -> Uri
fun getUriFromFilePath(context: Context, filePath: String): Uri? {
    val file = File(filePath)
    return if (file.exists()) {
        try {
            // FileProvider로 파일 검색후 Uri 생성
            val authority = "${context.packageName}.provider"
            FileProvider.getUriForFile(context, authority, file)
        } catch (e: IllegalArgumentException) {
            Log.d("whatisthis", "${context.packageName}ㅇㅔ러" + e.toString())
            e.printStackTrace()
            null
        }
    } else {
        null
    }
}