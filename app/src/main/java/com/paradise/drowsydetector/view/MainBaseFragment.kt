package com.paradise.drowsydetector.view

import com.paradise.drowsydetector.R
import com.paradise.drowsydetector.base.BaseViewbindingFragment
import com.paradise.drowsydetector.databinding.FragmentMainBaseBinding
import com.paradise.drowsydetector.view.home.HomeFragment


class MainBaseFragment : BaseViewbindingFragment<FragmentMainBaseBinding>(FragmentMainBaseBinding::inflate) {

    override fun onViewCreated() {
        // 초기 화면
        childFragmentManager
            .beginTransaction()
            .replace(R.id.mainbase_layout, HomeFragment(), "main")
            .commitAllowingStateLoss()
    }

}