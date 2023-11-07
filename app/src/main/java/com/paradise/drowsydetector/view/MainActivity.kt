package com.paradise.drowsydetector.view

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.gun0912.tedpermission.coroutine.TedPermission
import com.paradise.drowsydetector.R
import com.paradise.drowsydetector.base.BaseActivity
import com.paradise.drowsydetector.databinding.ActivityMainBinding
import com.paradise.drowsydetector.utils.ApplicationClass
import com.paradise.drowsydetector.utils.CUURRENTFRAGMENTTAG
import com.paradise.drowsydetector.utils.MAINBASE
import com.paradise.drowsydetector.viewmodel.ShelterViewModel
import kotlinx.coroutines.launch

class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main) {
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

    val shelterViewModel: ShelterViewModel by viewModels() {
        ShelterViewModel.ShelterViewModelFactory(ApplicationClass.getApplicationContext().relaxRepository)
    }

    override fun onCreate() {
        lifecycleScope.launch {
            val permissionResult = TedPermission.create()
                .setRationaleTitle(R.string.rationale_title)
                .setRationaleMessage(R.string.rationale_message)
                .setDeniedTitle("Permission denied")
                .setDeniedMessage(
                    "만약 권한을 허용해주시지 않으면 졸음 감지 서비스를 사용할 수 없습니다.\n\n[설정] > [권한 확인]에서 권한을 허용해주세요"
                )
                .setGotoSettingButtonText("설정")
                .setPermissions(
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                .check()

            Log.d("ted", "permissionResult: $permissionResult")
        }

        shelterViewModel.getAllShelter("경기도", "김포시")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(CUURRENTFRAGMENTTAG, MAINBASE)
    }
}