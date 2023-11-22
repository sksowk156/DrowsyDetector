package com.paradise.drowsydetector.view.home

import android.Manifest
import android.content.Intent
import android.os.Build
import android.util.Log
import com.paradise.drowsydetector.R
import com.paradise.drowsydetector.base.BaseViewbindingFragment
import com.paradise.drowsydetector.databinding.FragmentHomeBinding
import com.paradise.drowsydetector.service.AnalyzeeService
import com.paradise.drowsydetector.utils.ACTION_START_OR_RESUME_SERVICE
import com.paradise.drowsydetector.utils.ACTION_STOP_SERVICE
import com.paradise.drowsydetector.utils.ANALYZE
import com.paradise.drowsydetector.utils.SETTING
import com.paradise.drowsydetector.utils.STATISTIC
import com.paradise.drowsydetector.utils.checkPermissions
import com.paradise.drowsydetector.view.analyze.AnalyzeFragment
import com.paradise.drowsydetector.view.setting.SettingFragment
import com.paradise.drowsydetector.view.statistic.StatisticFragment

class HomeFragment : BaseViewbindingFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {
    override fun onViewCreated() {
        subscribeToObservers()

        binding.btHomeToanalyze.setOnAvoidDuplicateClick {
            checkPermissions(arrayOf(Manifest.permission.CAMERA)) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.layout_home_main, AnalyzeFragment(), ANALYZE).addToBackStack(null)
                    .commitAllowingStateLoss()
            }
        }

        binding.btHomeTosetting.setOnAvoidDuplicateClick {
            parentFragmentManager.beginTransaction()
                .replace(R.id.layout_home_main, SettingFragment(), SETTING).addToBackStack(null)
                .commitAllowingStateLoss()
        }

        binding.btHomeTostatistic.setOnAvoidDuplicateClick {
            parentFragmentManager.beginTransaction()
                .replace(R.id.layout_home_main, StatisticFragment(), STATISTIC).addToBackStack(null)
                .commitAllowingStateLoss()
        }
        binding.ivHomeLogo.setOnAvoidDuplicateClick {
            checkPermissions(arrayOf(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    checkPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS)) {
                        if (Build.VERSION.SDK_INT >= 34) {
                            checkPermissions(arrayOf(Manifest.permission.FOREGROUND_SERVICE_CAMERA)) {
                                Log.d("whatisthis", "?!")
                                toggleRun()
                            }
                        } else {
                            toggleRun()
                        }
                    }
                } else {
                    toggleRun()
                }
            }
        }

        binding.layoutHome1.setOnAvoidDuplicateClick {
            stopRun()
        }
    }

    private var isTracking = false // 러닝 상태
    private fun toggleRun() {
        if (isTracking) {
            sendCommandToService(ACTION_STOP_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), AnalyzeeService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    // 달리기 종료
    private fun stopRun() {
        sendCommandToService(ACTION_STOP_SERVICE)
        subscribeTrackingServiceKilled()
    }

    private fun subscribeTrackingServiceKilled() {
        AnalyzeeService.serviceKilled.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it == true) {
                    backPress()
                }
            }
        }
    }

    private fun subscribeToObservers() {
        AnalyzeeService.isTracking.observe(viewLifecycleOwner) {
            updateTracking(it)
        }
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
    }
//    // 카메라 권한 체크
//    private fun checkPermission(): Boolean {
//        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) ==
//                PackageManager.PERMISSION_GRANTED
//    }
//
//    // 카메라 권한 요청
//    private fun requestPermission() {
//        ActivityCompat.requestPermissions(
//            requireActivity(), arrayOf(Manifest.permission.CAMERA),
//            1
//        )
//    }
//
//    // 권한요청 결과
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray,
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            Log.d("whatisthis", "Permission success")
//        } else {
//            Log.d("whatisthis", "Fail")
//        }
//    }
}