package com.paradise.drowsydetector.utils

import android.content.Context
import android.location.Location
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.PointF3D
import com.google.mlkit.vision.facemesh.FaceMeshPoint
import com.paradise.drowsydetector.utils.ApplicationClass.Companion.getApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.lang.Math.PI
import java.lang.Math.asin
import java.lang.Math.atan2
import java.lang.Math.cos
import java.lang.Math.sin

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

const val MAINBASE = "homebase"
const val ANALYZE = "analye"
const val HOME = "home"
const val SETTING = "setting"
const val STATISTIC = "statistic"
const val CUURRENTFRAGMENTTAG = "currentfragment"


const val PREFERENCES_NAME = "my_preferences" // datasotre 이름
const val GUIDEMODE = "guide_mode_datastore" // 가까운 휴식 장소 안내를 받을지 안받을지(Boolean)
const val BASICMUSICMODE = "basic_music_mode_datastore" // 기본 음악을 들을지 개인 설정 음악을 들을지(Boolean)
const val MUSICVOLUME = "music_volume_datastore" // 음악의 볼륨(Int)
const val REFRESHMODE = "refresh_mode_datastore" // 환기 주기(Int)

const val DEFAULTNUMOFROWS = 300
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

// 두 지점 간의 거리를 미터로 반환
fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val location1 = Location("Point A").apply {
        latitude = lat1
        longitude = lon1
    }

    val location2 = Location("Point B").apply {
        latitude = lat2
        longitude = lon2
    }
    return location1.distanceTo(location2)
}

fun calRatio(upDownAngle: Float, leftRightAngle: Float, landmark: List<FaceMeshPoint>): Double {
    val upDownSec =
        (1 / Math.cos(Math.toRadians(upDownAngle.toDouble()))) //  val upDownRadian = upDownAngle * Math.PI / 180.0
    var leftRightSec =
        (1 / Math.cos(Math.toRadians(leftRightAngle.toDouble()))) // val leftRightRadian = leftRightAngle * Math.PI / 180.0

    val rightUpper = landmark.get(159).position
    val rightLower = landmark.get(145).position

    val leftUpper = landmark.get(386).position
    val leftLower = landmark.get(374).position

    var widthLower = (calDist(rightLower, leftLower)) * leftRightSec
    var heightAvg = (calDist(rightUpper, rightLower) + calDist(leftUpper, leftLower)) / 2.0

    if (upDownAngle < 0) { // 카메라가 위에 있을 경우
        heightAvg *= (upDownSec * 1.1) // 랜드마크의 세로 길이가 짧게 측정되는 경향이 있어 값을 보정
    } else { // 카메라가 아래에 있을 경우
        heightAvg *= (upDownSec * 0.9) // 랜드마크의 세로 길이가 짧게 측정되는 경향이 있어 값을 보정
    }

    // 종횡비 계산
    return (heightAvg / widthLower)
}

fun calDist(point1: PointF3D, point2: PointF3D): Double {
    val dx = point1.x - point2.x
    val dy = point1.y - point2.y
    return Math.sqrt((dx * dx + dy * dy).toDouble())
}
data class BoundingBox(val minLatitude: Double, val minLongitude: Double, val maxLatitude: Double, val maxLongitude: Double)
fun getBoundingBox(latitude: Double, longitude: Double, radiusInKm: Double): BoundingBox {
    val earthRadius = 6371.0 // Earth radius in kilometers

    val latRadians = Math.toRadians(latitude)
    val lonRadians = Math.toRadians(longitude)

    val deltaLat = radiusInKm / earthRadius
    val deltaLon = asin(sin(deltaLat) / cos(latRadians))

    val minLat = latRadians - deltaLat
    val minLon = lonRadians - deltaLon

    val maxLat = latRadians + deltaLat
    val maxLon = lonRadians + deltaLon

    val minLatDegrees = Math.toDegrees(minLat)
    val minLonDegrees = Math.toDegrees(minLon)
    val maxLatDegrees = Math.toDegrees(maxLat)
    val maxLonDegrees = Math.toDegrees(maxLon)

    return BoundingBox(minLatDegrees, minLonDegrees, maxLatDegrees, maxLonDegrees)
}
