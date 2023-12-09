package com.paradise.common.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.core.model.BoundingBox
import com.paradise.common.network.mainDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.Random


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
    val deltaLon = Math.asin(Math.sin(deltaLat) / Math.cos(latRadians))

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


