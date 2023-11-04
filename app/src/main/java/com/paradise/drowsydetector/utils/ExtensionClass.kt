package com.paradise.drowsydetector.utils

import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.paradise.drowsydetector.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import reactivecircus.flowbinding.android.view.clicks
import reactivecircus.flowbinding.android.widget.afterTextChanges

/**
 * Set on finish input flow, textview 입력 완성 후 이벤트 처리 메서드
 *
 * flow의 debounce() 안에 sleep 타임은 1초로 설정되어 있음, 1초 뒤에야 현재의 text를 반환 받을 수 있다.
 *
 * flowBinding으로 구현
 * @param actionInMainThread
 * @receiver
 * @author 진혁
 */
fun TextView.setOnFinishInputFlow(actionInMainThread: (completedText: String) -> Unit) {
    this.afterTextChanges()
        .flowOn(Dispatchers.Main) // afterTextChanges()를 main에서 실행
        .debounce(INPUT_COMPLETE_TIME) // debounce()를 io에서 실행
        .flowOn(Dispatchers.IO)
        .onEach {// onEach{}를 main에서 실행
            actionInMainThread(it.view.text.toString())
        }.launchIn(mainScope)
}

/**
 * Set on avoid duplicate click, view에 대한 중복 클릭 방지 이벤트 처리 메서드
 *
 * throttleFrist() 안에 sleep 타임은 0.3초로 설정되어 있음, 0.3초간 클릭 못함
 * @param actionInMainThread
 * @receiver
 * @author 진혁
 */
fun View.setOnAvoidDuplicateClickFlow(actionInMainThread: () -> Unit) {
    this.clicks()
        .flowOn(Dispatchers.Main) // 이후 chain의 메서드들은 쓰레드 io 영역에서 처리
        .throttleFirst(CLICK_INTERVAL_TIME)
        .flowOn(Dispatchers.IO) // 이후 chain의 메서드들은 쓰레드 main 영역에서 처리
        .onEach {// onEach{}를 main에서 실행
            actionInMainThread()
        }.launchIn(mainScope)
}

// throttleFirst()는 Flow에 없기 때문에 직접 구현해줘야 한다. debounce()는 있다.
private fun <T> Flow<T>.throttleFirst(intervalTime: Long): Flow<T> = flow {
    var throttleTime = 0L
    collect { upStream ->
        val currentTime = System.currentTimeMillis()
        if ((currentTime - throttleTime) > intervalTime) {
            throttleTime = currentTime
            emit(upStream)
        }
    }
}

/**
 * Set on menu item click listener flow binding, menuItem 클릭 이벤트 처리
 * menuItem 클릭 이벤트 처리를 flowBinding으로 처리함
 * @param actionInMainThread
 * @receiver
 * @author 진혁
 */
private fun MenuItem.setOnMenuItemClickListenerFlowBinding(actionInMainThread: () -> Unit) {
    this.clicks { it.isEnabled } // 'isChecked'는 현재 displaying a check mark 인지 확인하는 것이므로, 여기선 'isEnabled'로 사용가능한지 확인하는 것이 맞다
        .onEach {
            actionInMainThread()
        }.launchIn(mainScope)
}

/**
 * Inflate reset menu, 툴바에 '리셋' 메뉴 버튼을 삽입할 때 이미지, 이벤트 리스너 자동 등록 메서드
 *
 * @param editListener, 리셋 메뉴 버튼 이벤트 처리
 * @receiver
 */
fun Toolbar.inflateResetMenu(editListener: (() -> Unit)) {
    this.inflateMenu(R.menu.menu_reset)
    val menuItem = this.menu.findItem(R.id.reset_menu_standardreset)
    menuItem.setOnMenuItemClickListenerFlowBinding {
        editListener()
        showToast("?")
    }
}