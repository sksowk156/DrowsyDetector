package com.paradise.drowsydetector.view.home

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.paradise.drowsydetector.R
import com.paradise.drowsydetector.base.BaseViewbindingFragment
import com.paradise.drowsydetector.databinding.FragmentHomeBinding
import com.paradise.drowsydetector.utils.showToast
import com.paradise.drowsydetector.view.analyze.AnalyzeFragment

class HomeFragment :
    BaseViewbindingFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    override fun onViewCreated() {
        binding.homeTextStartanalyze.setOnClickListener {
            if (checkPermission()) {
                parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.mainbase_layout, AnalyzeFragment(), "analye")
                    .addToBackStack(null)
                    .commitAllowingStateLoss()
            } else {
                showToast("권한 허가 필요")
                requestPermission()
            }
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