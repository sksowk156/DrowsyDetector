package com.paradise.drowsydetector.view

import com.paradise.drowsydetector.R
import com.paradise.drowsydetector.base.BaseFragment
import com.paradise.drowsydetector.databinding.FragmentMainBaseBinding
import com.paradise.drowsydetector.view.home.HomeFragment


class MainBaseFragment : BaseFragment<FragmentMainBaseBinding>(R.layout.fragment_main_base) {

    override fun savedatainit() {
        // 초기 화면
        childFragmentManager
            .beginTransaction()
            .replace(R.id.mainbase_layout, HomeFragment(), "main")
            .commitAllowingStateLoss()
    }

    override fun init() {

    }

}