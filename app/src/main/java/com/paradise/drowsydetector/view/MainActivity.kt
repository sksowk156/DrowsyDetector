package com.paradise.drowsydetector.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil.setContentView
import com.paradise.drowsydetector.R
import com.paradise.drowsydetector.base.BaseActivity
import com.paradise.drowsydetector.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main) {
    override fun savedatainit() {
        supportFragmentManager
            .beginTransaction()
            .replace(binding.mainFramelayout.id, MainBaseFragment(), "mainbase")
            .commitAllowingStateLoss()
    }

    override fun init() {
    }
}