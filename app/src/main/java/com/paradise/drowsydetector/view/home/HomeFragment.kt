package com.paradise.drowsydetector.view.home

import android.Manifest
import androidx.fragment.app.activityViewModels
import com.paradise.drowsydetector.R
import com.paradise.drowsydetector.base.BaseViewbindingFragment
import com.paradise.drowsydetector.databinding.FragmentHomeBinding
import com.paradise.drowsydetector.utils.ANALYZE
import com.paradise.drowsydetector.utils.SETTING
import com.paradise.drowsydetector.utils.STATISTIC
import com.paradise.drowsydetector.utils.checkPermissions
import com.paradise.drowsydetector.view.analyze.AnalyzeFragment
import com.paradise.drowsydetector.view.setting.SettingFragment
import com.paradise.drowsydetector.view.statistic.StatisticFragment
import com.paradise.drowsydetector.viewmodel.AnalyzeViewModel

class HomeFragment :
    BaseViewbindingFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    val analyzeViewModel: AnalyzeViewModel by activityViewModels()
    override fun onViewCreated() {
        binding.btHomeToanalyze.setOnAvoidDuplicateClick {
            checkPermissions(arrayOf(Manifest.permission.CAMERA)) {
                parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.layout_home_main, AnalyzeFragment(), ANALYZE)
                    .addToBackStack(null)
                    .commitAllowingStateLoss()
            }
        }

        binding.btHomeTosetting.setOnAvoidDuplicateClick {
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.layout_home_main, SettingFragment(), SETTING)
                .addToBackStack(null)
                .commitAllowingStateLoss()
        }

        binding.btHomeTostatistic.setOnAvoidDuplicateClick {
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.layout_home_main, StatisticFragment(), STATISTIC)
                .addToBackStack(null)
                .commitAllowingStateLoss()
        }
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