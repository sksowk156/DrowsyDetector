package com.paradise.drowsydetector.view.statistic

import com.paradise.drowsydetector.base.BaseViewbindingFragment
import com.paradise.drowsydetector.databinding.FragmentStatisticBinding

class StatisticFragment : BaseViewbindingFragment<FragmentStatisticBinding>(FragmentStatisticBinding::inflate) {
    override fun onViewCreated() {
        binding.toolbarStatistic.setToolbarMenu("통계", true)
    }

}