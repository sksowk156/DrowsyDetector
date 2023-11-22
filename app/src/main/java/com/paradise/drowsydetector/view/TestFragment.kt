//package com.paradise.drowsydetector.view
//
//import android.content.Intent
//import com.paradise.drowsydetector.base.BaseViewbindingFragment
//import com.paradise.drowsydetector.databinding.FragmentTestBinding
//import com.paradise.drowsydetector.service.AnalyzeeService
//import com.paradise.drowsydetector.utils.ACTION_START_OR_RESUME_SERVICE
//import com.paradise.drowsydetector.utils.ACTION_STOP_SERVICE
//
//
//class TestFragment : BaseViewbindingFragment<FragmentTestBinding>(FragmentTestBinding::inflate) {
//    override fun onViewCreated() {
////        subscribeToObservers()
////        toggleRun()
//    }
//
//    private fun updateTracking(isTracking: Boolean) { // 서비스 상태 변경에 따른 ui 변경
//        this.isTracking = isTracking
//        if (!isTracking) {
//        } else if (isTracking) {
//        }
//    }
//
//    // 분석 시작
//    private fun toggleRun() {
//        if (isTracking) {
//            stopRun()
//        } else {
//            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
//        }
//    }
//
//    private fun sendCommandToService(action: String) =
//        Intent(requireContext(), AnalyzeeService::class.java).also {
//            it.action = action
//            requireContext().startService(it)
//        }
//
//
//    // 달리기 종료
//    private fun stopRun() {
//        sendCommandToService(ACTION_STOP_SERVICE)
//    }
//
//
//}