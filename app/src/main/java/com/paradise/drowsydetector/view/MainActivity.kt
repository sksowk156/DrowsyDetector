package com.paradise.drowsydetector.view

import android.Manifest
import android.os.Bundle
import androidx.activity.viewModels
import com.paradise.drowsydetector.R
import com.paradise.drowsydetector.base.BaseActivity
import com.paradise.drowsydetector.databinding.ActivityMainBinding
import com.paradise.drowsydetector.utils.ApplicationClass
import com.paradise.drowsydetector.utils.CUURRENTFRAGMENTTAG
import com.paradise.drowsydetector.utils.GUIDEMODE
import com.paradise.drowsydetector.utils.MAINBASE
import com.paradise.drowsydetector.utils.checkPermissions
import com.paradise.drowsydetector.viewmodel.AnalyzeViewModel
import com.paradise.drowsydetector.viewmodel.MusicViewModel
import com.paradise.drowsydetector.viewmodel.SettingViewModel

class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main) {

    val analyzeViewModel: AnalyzeViewModel by viewModels() {
        AnalyzeViewModel.AnalyzeViewModelFactory(ApplicationClass.getApplicationContext().relaxRepository)
    }

    val musicViewModel: MusicViewModel by viewModels() {
        MusicViewModel.MusicViewModelFactory(ApplicationClass.getApplicationContext().musicRepository)
    }

    val settingViewModel: SettingViewModel by viewModels() {
        SettingViewModel.SettingViewModelFactory(ApplicationClass.getApplicationContext().settingRepository)
    }

    override fun onCreate() {
        checkPermissions(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            showToast("권한 허용")
//            analyzeViewModel.getAllParkingLot2("충청남도", "부여군", 36.2770825820, 126.9110578117)
        }

        settingViewModel.getSettingMode(GUIDEMODE)
        musicViewModel.getAllMusic()
    }

    override fun saveInstanceStateNull() {
        supportFragmentManager
            .beginTransaction()
            .add(binding.mainFramelayout.id, MainBaseFragment(), MAINBASE)
            .commitAllowingStateLoss()
    }

    override fun saveInstanceStateNotNull(bundle: Bundle) {
        val currentFragmentTag = bundle.getString(CUURRENTFRAGMENTTAG).toString()
        supportFragmentManager
            .beginTransaction()
            .show(supportFragmentManager.findFragmentByTag(currentFragmentTag)!!)
            .commitAllowingStateLoss()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(CUURRENTFRAGMENTTAG, MAINBASE)
    }
}