package com.paradise.drowsydetector.view.home

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.paradise.drowsydetector.R
import com.paradise.drowsydetector.base.BaseViewbindingFragment
import com.paradise.drowsydetector.databinding.FragmentHomeBinding
import com.paradise.drowsydetector.utils.ANALYZE
import com.paradise.drowsydetector.utils.ApplicationClass
import com.paradise.drowsydetector.utils.ResponseState
import com.paradise.drowsydetector.utils.SETTING
import com.paradise.drowsydetector.utils.STATISTIC
import com.paradise.drowsydetector.utils.getBoundingBox
import com.paradise.drowsydetector.utils.showToast
import com.paradise.drowsydetector.view.analyze.AnalyzeFragment
import com.paradise.drowsydetector.view.setting.SettingFragment
import com.paradise.drowsydetector.view.statistic.StatisticFragment
import com.paradise.drowsydetector.viewmodel.AnalyzeViewModel
import kotlinx.coroutines.launch

class HomeFragment :
    BaseViewbindingFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    val analyzeViewModel: AnalyzeViewModel by viewModels() {
        AnalyzeViewModel.AnalyzeViewModelFactory(ApplicationClass.getApplicationContext().relaxRepository)
    }

    override fun onViewCreated() {
        binding.btHomeToanalyze.setOnAvoidDuplicateClick {
            if (checkPermission()) {
                parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.layout_home_main, AnalyzeFragment(), ANALYZE)
                    .addToBackStack(null)
                    .commitAllowingStateLoss()
            } else {
                showToast("권한 허가 필요")
                requestPermission()
            }
        }

        binding.btHomeTosetting.setOnAvoidDuplicateClick {
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.layout_home_main, SettingFragment(), SETTING)
                .addToBackStack(null)
                .commitAllowingStateLoss()
        }

        val temp = getBoundingBox(36.36564901, 127.42444290, 5.0)
//        analyzeViewModel.getAllParkingLot2(temp)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                analyzeViewModel.parkingLots.collect {
                    when (it) {
                        is ResponseState.Loading -> {
                            Log.d("whatisthis", "로딩")
                        }

                        is ResponseState.Success -> {
                            Log.d("whatisthis", it.data.toString())
                        }

                        is ResponseState.Fail -> {
                            Log.d("whatisthis", it.message.toString() + it.code)
                        }

                        is ResponseState.Error -> {
                            Log.d("whatisthis", it.exception.toString())
                        }
                    }
                }
            }
        }

//        lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//
//            }
//        }
//        analyzeViewModel.get()
//        analyzeViewModel.temp.observe(viewLifecycleOwner) {
//            if (it.isSuccessful) {
//                Log.d("whatisthis", it.body()?.response?.body?.items.toString())
//            }
//        }

//        analyzeViewModel.getAllParkingLot()
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                analyzeViewModel.allParkingLot.collect {
//                    when (it) {
//                        is ResponseState.Loading -> {
//                            Log.d("whatisthis", "로딩")
//                        }
//
//                        is ResponseState.Success -> {
//                            Log.d("whatisthis", it.data.toString())
////                            val tempLocation = Location(LocationManager.GPS_PROVIDER)
////                            var minDist = 100000000f
////                            var result: Item? = null
////                            for (i in it.data.response.body.items) {
////                                tempLocation.longitude = (i.longitude).toDouble()
////                                tempLocation.latitude = (i.latitude).toDouble()
////                                val dist =
////                                    tempLocation.distanceTo(AnalyzeViewModel.location!!.tempLocation)
////                                if (minDist > dist) {
////                                    minDist = dist
////                                    result = i
////                                }
////                            }
////                            Log.d("whatisthis", "reuslt : " + result?.lnmadr.toString())
//                        }
//
//                        is ResponseState.Fail -> {
//                            Log.d("whatisthis", it.message.toString() + it.code)
//                        }
//
//                        is ResponseState.Error -> {
//                            Log.d("whatisthis", it.exception.toString())
//                        }
//                    }
//                }
//            }
//        }


        binding.btHomeTostatistic.setOnAvoidDuplicateClick {
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.layout_home_main, StatisticFragment(), STATISTIC)
                .addToBackStack(null)
                .commitAllowingStateLoss()
        }
    }

    // 카메라 권한 체크
    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
    }

    // 카메라 권한 요청
    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(), arrayOf(Manifest.permission.CAMERA),
            1
        )
    }

    // 권한요청 결과
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("whatisthis", "Permission success")
        } else {
            Log.d("whatisthis", "Fail")
        }
    }
}