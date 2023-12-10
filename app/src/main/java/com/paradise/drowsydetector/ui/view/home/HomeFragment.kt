//package com.paradise.drowsydetector.ui.view.home
//
//import com.paradise.drowsydetector.base.BaseViewbindingFragment
//import com.paradise.drowsydetector.databinding.FragmentHomeBinding
//
//class HomeFragment : BaseViewbindingFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {
//    override fun onViewCreated() {
////        binding.btHomeToanalyze.setOnAvoidDuplicateClick {
////            checkPermissions(arrayOf(Manifest.permission.CAMERA)) {
////                parentFragmentManager.beginTransaction()
////                    .replace(R.id.layout_home_main, AnalyzeFragment(), ANALYZE).addToBackStack(null)
////                    .commitAllowingStateLoss()
////            }
////        }
////
////        binding.btHomeTosetting.setOnAvoidDuplicateClick {
////            parentFragmentManager.beginTransaction()
////                .replace(R.id.layout_home_main, SettingFragment(), SETTING).addToBackStack(null)
////                .commitAllowingStateLoss()
////        }
////
////        binding.btHomeTostatistic.setOnAvoidDuplicateClick {
////            parentFragmentManager.beginTransaction()
////                .replace(R.id.layout_home_main, StatisticFragment(), STATISTIC).addToBackStack(null)
////                .commitAllowingStateLoss()
////        }
//    }
//
//
////    // 카메라 권한 체크
////    private fun checkPermission(): Boolean {
////        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) ==
////                PackageManager.PERMISSION_GRANTED
////    }
////
////    // 카메라 권한 요청
////    private fun requestPermission() {
////        ActivityCompat.requestPermissions(
////            requireActivity(), arrayOf(Manifest.permission.CAMERA),
////            1
////        )
////    }
////
////    // 권한요청 결과
////    override fun onRequestPermissionsResult(
////        requestCode: Int,
////        permissions: Array<out String>,
////        grantResults: IntArray,
////    ) {
////        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
////        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
////            Log.d("whatisthis", "Permission success")
////        } else {
////            Log.d("whatisthis", "Fail")
////        }
////    }
//}