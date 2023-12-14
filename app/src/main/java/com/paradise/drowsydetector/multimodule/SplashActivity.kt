package com.paradise.drowsydetector.multimodule

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.paradise.common_ui.base.BaseActivity
import com.paradise.drowsydetector.R
import com.paradise.drowsydetector.databinding.ActivitySplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>(R.layout.activity_splash) {
    override fun onCreate() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // status bar 영역까지 이미지 확장
        window.statusBarColor = Color.TRANSPARENT
        lifecycleScope.launch {
            delay(2000)
            // 로그인 안되어있을 때 로그인 페이지 열림
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}