package com.paradise.drowsydetector.utils

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.location.Location
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.mlkit.vision.common.PointF3D
import com.google.mlkit.vision.facemesh.FaceMeshPoint
import com.paradise.drowsydetector.data.remote.parkinglot.Item
import com.paradise.drowsydetector.utils.ApplicationClass.Companion.getApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.lang.Math.asin
import java.lang.Math.cos
import java.lang.Math.sin
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.Random

// BaseFragment에서 사용하는 typealias
typealias FragmentInflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

// Notification
const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
const val NOTIFICATION_CHANNEL_NAME = "Tracking"
const val NOTIFICATION_ID = 199 // 0 하면 안됨!!!
const val ACTION_SHOW_TRACKING_FRAGMENT = "ACTION_SHOW_TRACKING_FRAGMENT"

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

enum class DAY { WEEKDAY, SAT, HOLIDAY }

fun getTodayDate(): String {
    val currentDate = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    return currentDate.format(formatter)
}

fun getDayType(): DAY = when (LocalDate.now().dayOfWeek) {
    in DayOfWeek.MONDAY..DayOfWeek.FRIDAY -> DAY.WEEKDAY
    DayOfWeek.SATURDAY -> DAY.SAT
    DayOfWeek.SUNDAY -> DAY.HOLIDAY
    else -> DAY.WEEKDAY
}

fun getCurrentTime(): String =
    SimpleDateFormat("HH", Locale.getDefault()).format(Calendar.getInstance().time)


fun isInTime(day: DAY, nowTime: String, item: Item) = when (day) {
    DAY.WEEKDAY -> {
        compareTime(nowTime, item.weekdayOperOpenHhmm, item.weekdayOperColseHhmm)
    }

    DAY.SAT -> {
        compareTime(nowTime, item.satOperOperOpenHhmm, item.satOperCloseHhmm)
    }

    DAY.HOLIDAY -> {
        compareTime(nowTime, item.holidayOperOpenHhmm, item.holidayCloseOpenHhmm)
    }
}


fun compareTime(nowTime: String, openTime: String?, closeTime: String?) =
    if (openTime == null || closeTime == null) true // 시간이 등록되어 있지 않았을 경우
    else {
        if (openTime.equals("00:00") && closeTime.equals("23:59")) {
            true
        } else {
            nowTime.substring(0, 2).toInt() in openTime.substring(0, 2)
                .toInt()..closeTime.substring(
                0, 2
            ).toInt()
        }
    }

// 웃음 인식 임계값
const val SMILE_THREDHOLD = 0.9

// 졸음 인식 EAR 임계값
const val DROWSY_THREDHOLD = 0.8

// 졸음 인식 지속 시간 임계값
const val TIME_THREDHOLD = 1500

// 좌우 회전 각도 임계값
const val LEFT_RIGHT_ANGLE_THREDHOLD = 55

// 위,아래 회전 각도 임계값
const val UP_DOWN_ANGLE_THREDHOLD = 40

// 얼굴 각도 및 기준점 설정 상태
const val STANDARD_IN_ANGLE = 1 // 기준점을 설정할 수 있는 각도일 때, 측정 각도를 벗어나지 않았을 때
const val OUT_OF_ANGLE = 2 // 측정 각도를 벗어났을 때
const val NO_STANDARD = 3 // 아직 기준점이 없을 때

const val PREFERENCES_NAME = "my_preferences" // datasotre 이름
const val GUIDEMODE = "guide_mode_datastore" // 가까운 휴식 장소 안내를 받을지 안받을지(Boolean)
const val BASICMUSICMODE = "basic_music_mode_datastore" // 기본 음악을 들을지 개인 설정 음악을 들을지(Boolean)
const val MUSICVOLUME = "music_volume_datastore" // 음악의 볼륨(Int)
const val REFRESHTERM = "refresh_term_datastore" // 환기 주기(Int)

// 한번에 가져오는 주차장 정보 개수
const val DEFAULT_NUM_OF_ROWS = 200

// 기본 음악 지속 시간
const val DEFAULT_MUSIC_DURATION: Long = 3000
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
fun Location.calculateDistance(lat1: Double, lon1: Double) =
    this.distanceTo(Location("Point").apply {
        latitude = lat1
        longitude = lon1
    })


fun calRatio(upDownAngle: Float, leftRightAngle: Float, landmark: List<FaceMeshPoint>): Double {
    val upDownSec =
        (1 / cos(Math.toRadians(upDownAngle.toDouble()))) //  val upDownRadian = upDownAngle * Math.PI / 180.0
    var leftRightSec =
        (1 / cos(Math.toRadians(leftRightAngle.toDouble()))) // val leftRightRadian = leftRightAngle * Math.PI / 180.0

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

fun checkHeadAngleInNoStandard(upDownAngle: Float, leftRightAngle: Float) =
    upDownAngle < 4 && upDownAngle > -4 && leftRightAngle < 4 && leftRightAngle > -4

fun isInLeftRight(leftRightAngle: Float) = leftRightAngle < 4 && leftRightAngle > -4

fun checkHeadAngleInStandard(leftRightAngle: Float, upDownAngle: Float) =
    leftRightAngle < -LEFT_RIGHT_ANGLE_THREDHOLD || leftRightAngle > LEFT_RIGHT_ANGLE_THREDHOLD || upDownAngle < -UP_DOWN_ANGLE_THREDHOLD || upDownAngle > UP_DOWN_ANGLE_THREDHOLD

/**
 * Launch with repeat on lifecycle
 *
 * 스코프 코드가 반복되어서 확장 함수를 생성했다.
 * @param state
 * @param dispatcher, 기본 Disapatcher.Main이다.
 * @param block
 * @receiver
 */
fun LifecycleOwner.launchWithRepeatOnLifecycle(
    state: Lifecycle.State,
    dispatcher: CoroutineDispatcher = mainDispatcher,
    block: suspend () -> Unit,
) = this.lifecycleScope.launch(dispatcher) {
    this@launchWithRepeatOnLifecycle.repeatOnLifecycle(state) {
        block()
    }
}


/**
 * Get random element
 *
 * 리스트에서 랜덤으로 원소를 뽑아준다.
 * @param T
 * @return
 */
fun <T> List<T>.getRandomElement(): T? {
    if (this.isEmpty()) return null
    val randomIndex = Random().nextInt(this.size)
    return get(randomIndex)
}

const val DEFAULT_RADIUSKM = 10.0

/**
 * Get bounding box
 *
 * 입력받은 lat과 lon을 기준으로 동,서,남,북 radiusInKm까지의 위, 경도 범위를 반환해줌
 * @param latitude, 현재 위도
 * @param longitude, 현재 경도
 * @param radiusInKm, 탐색할 범위
 * @return 위, 경도 최소, 최대범위
 */

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

/**
 * Bounding box
 *
 * getBoundingBox의 결과 값
 * @property minLatitude
 * @property minLongitude
 * @property maxLatitude
 * @property maxLongitude
 * @constructor Create empty Bounding box
 */
data class BoundingBox(
    val minLatitude: Double,
    val minLongitude: Double,
    val maxLatitude: Double,
    val maxLongitude: Double,
)

/**
 * Beep
 *
 * 사용법
 *
 * beep(ToneGenerator.TONE_CDMA_ABBR_ALERT,500, ToneGenerator.MAX_VOLUME)
 *
 * ! MusicHelper로 대체
 * @param mediaFileRawId
 * @param duration
 * @param volume
 */
fun beep(mediaFileRawId: Int, duration: Int, volume: Int) {
    val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, volume)
    toneGenerator.startTone(mediaFileRawId, duration)

    val handler = Handler(Looper.getMainLooper())
    handler.postDelayed(Runnable {
        toneGenerator.release()
    }, 200)
}