package com.paradise.drowsydetector.view.setting

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.paradise.drowsydetector.R
import com.paradise.drowsydetector.base.BaseViewbindingFragment
import com.paradise.drowsydetector.databinding.FragmentSettingBinding
import com.paradise.drowsydetector.utils.inflateResetMenu

class SettingFragment :
    BaseViewbindingFragment<FragmentSettingBinding>(FragmentSettingBinding::inflate) {
    override fun onViewCreated() {
        binding.toolbarSetting.setToolbarMenu("설정", true)
    }

}