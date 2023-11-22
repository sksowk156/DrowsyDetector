package com.paradise.drowsydetector.view

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.paradise.drowsydetector.R
import com.paradise.drowsydetector.base.BaseActivity
import com.paradise.drowsydetector.databinding.ActivityMainBinding
import com.paradise.drowsydetector.utils.ACTION_SHOW_TRACKING_FRAGMENT
import com.paradise.drowsydetector.utils.ApplicationClass
import com.paradise.drowsydetector.utils.BASICMUSICMODE
import com.paradise.drowsydetector.utils.CUURRENTFRAGMENTTAG
import com.paradise.drowsydetector.utils.GUIDEMODE
import com.paradise.drowsydetector.utils.MAINBASE
import com.paradise.drowsydetector.utils.checkPermissions
import com.paradise.drowsydetector.utils.defaultDispatcher
import com.paradise.drowsydetector.utils.launchWithRepeatOnLifecycle
import com.paradise.drowsydetector.viewmodel.AnalyzeViewModel
import com.paradise.drowsydetector.viewmodel.MusicViewModel
import com.paradise.drowsydetector.viewmodel.SettingViewModel
import com.paradise.drowsydetector.viewmodel.StaticsViewModel

class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main) {

    val analyzeViewModel: AnalyzeViewModel by viewModels() {
        AnalyzeViewModel.AnalyzeViewModelFactory(ApplicationClass.getApplicationContext().relaxRepository)
    }

    val staticsViewModel: StaticsViewModel by viewModels() {
        StaticsViewModel.StaticsViewModelFactory(ApplicationClass.getApplicationContext().staticRepository)
    }

    val musicViewModel: MusicViewModel by viewModels() {
        MusicViewModel.MusicViewModelFactory(ApplicationClass.getApplicationContext().musicRepository)
    }

    val settingViewModel: SettingViewModel by viewModels() {
        SettingViewModel.SettingViewModelFactory(ApplicationClass.getApplicationContext().settingRepository)
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?) {
        if (intent?.action == ACTION_SHOW_TRACKING_FRAGMENT) {
//            supportFragmentManager.beginTransaction()
//                .add(binding.homeFramelayout.id, HomeBaseFragment(), "homebase")
//                .addToBackStack(null)
//                .commit()
        }
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
            analyzeViewModel.checkDrowsy
        }
        staticsViewModel.getAllRecord()
        settingViewModel.getSettingModeBool(GUIDEMODE)
        settingViewModel.getSettingModeBool(BASICMUSICMODE)
        settingViewModel.getAllSetting()
        musicViewModel.getAllMusic()

        this.launchWithRepeatOnLifecycle(
            state = Lifecycle.State.STARTED,
            dispatcher = defaultDispatcher
        ) {
            staticsViewModel.allAnalyzeRecord.collect {
                if (it != null) {
                    Log.d("whatisthis",it.toString())
                }
            }
        }
        navigateToTrackingFragmentIfNeeded(intent)
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