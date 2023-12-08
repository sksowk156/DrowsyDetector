package com.paradise.drowsydetector.ui.view

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import com.paradise.drowsydetector.R
import com.paradise.drowsydetector.base.BaseActivity
import com.paradise.drowsydetector.databinding.ActivityMainBinding
import com.paradise.drowsydetector.ui.viewmodel.AnalyzeViewModel
import com.paradise.drowsydetector.ui.viewmodel.MusicViewModel
import com.paradise.drowsydetector.ui.viewmodel.SettingViewModel
import com.paradise.drowsydetector.ui.viewmodel.StaticsViewModel
import com.paradise.drowsydetector.utils.ACTION_SHOW_ANALYZING_FRAGMENT
import com.paradise.drowsydetector.utils.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main) {

    val analyzeViewModel: AnalyzeViewModel by viewModels()
//    {
//        AnalyzeViewModel.AnalyzeViewModelFactory(ApplicationClass.getApplicationContext().relaxRepositoryImpl)
//    }

    val staticsViewModel: StaticsViewModel by viewModels()
//    {
//        StaticsViewModel.StaticsViewModelFactory(ApplicationClass.getApplicationContext().staticRepository)
//    }

    val musicViewModel: MusicViewModel by viewModels()
//    {
//        MusicViewModel.MusicViewModelFactory(ApplicationClass.getApplicationContext().musicRepositoryImpl)
//    }

    val settingViewModel: SettingViewModel by viewModels()
//    {
//        SettingViewModel.SettingViewModelFactory(ApplicationClass.getApplicationContext().settingRepositoryImpl)
//    }

//    @Inject
//    lateinit var toastHelper: ToastHelper

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?) {
        if (intent?.action == ACTION_SHOW_ANALYZING_FRAGMENT) {
//            supportFragmentManager.beginTransaction()
//                .add(binding.homeFramelayout.id, MainBaseFragment(), "homebase")
//                .addToBackStack(null)
//                .commit()
        }
    }

    override fun onCreate() {
//        toastHelper.showToast("성공")
//        val navHostFragment =
//            supportFragmentManager.findFragmentById(R.id.main_framelayout) as NavHostFragment
//        val navController = navHostFragment.navController

        checkPermissions(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        ) {
            showToast("권한 허용")
            analyzeViewModel.checkDrowsy
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            checkPermissions(arrayOf(Manifest.permission.FOREGROUND_SERVICE)) {}
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS)) {}
        }

        checkPermissions(arrayOf(Manifest.permission.RECORD_AUDIO)) {}

        checkPermissions(arrayOf(Manifest.permission.SYSTEM_ALERT_WINDOW)) {}

        staticsViewModel.getAllRecord()
        settingViewModel.getSettingModeBool(GUIDEMODE)
        settingViewModel.getSettingModeInt(MUSICVOLUME)
        settingViewModel.getSettingModeBool(BASICMUSICMODE)
        settingViewModel.getAllSetting()
        musicViewModel.getAllMusic()

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