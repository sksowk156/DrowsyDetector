package com.paradise.drowsydetector.view

import com.paradise.drowsydetector.R
import com.paradise.drowsydetector.base.BaseViewbindingFragment
import com.paradise.drowsydetector.databinding.FragmentMainBaseBinding
import com.paradise.drowsydetector.utils.HOME
import com.paradise.drowsydetector.view.home.HomeFragment


class MainBaseFragment : BaseViewbindingFragment<FragmentMainBaseBinding>(FragmentMainBaseBinding::inflate) {

    override fun onViewCreated() {
    }

    override fun savedInstanceStateNull() {
        super.savedInstanceStateNull()
        // 초기 화면
        childFragmentManager
            .beginTransaction()
            .add(R.id.mainbase_layout, HomeFragment(), HOME) // homefragment에서 homelayout을 replace 해버리면 activity에서 못찾기 때문
            .commitAllowingStateLoss()
    }

}