package com.paradise.drowsydetector.view.statistic

import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import com.paradise.drowsydetector.base.BaseViewbindingFragment
import com.paradise.drowsydetector.databinding.FragmentStatisticBinding
import com.paradise.drowsydetector.utils.defaultDispatcher
import com.paradise.drowsydetector.utils.getTodayDate
import com.paradise.drowsydetector.utils.launchWithRepeatOnLifecycle
import com.paradise.drowsydetector.viewmodel.StaticsViewModel

class StatisticFragment :
    BaseViewbindingFragment<FragmentStatisticBinding>(FragmentStatisticBinding::inflate) {
    private val staticsViewModel: StaticsViewModel by activityViewModels()

    override fun onViewCreated() {
        binding.toolbarStatistic.setToolbarMenu("통계", true)

        subscribeTodayRecord()
        staticsViewModel.getRecord(getTodayDate())
    }

    private fun subscribeTodayRecord() {
        viewLifecycleOwner.launchWithRepeatOnLifecycle(
            state = Lifecycle.State.STARTED,
            dispatcher = defaultDispatcher
        ) {
            staticsViewModel.drowsyRecord.collect {
                staticsViewModel.getWinkCount(it.id)
                staticsViewModel.getDrowsyCount(it.id)
            }
        }

        viewLifecycleOwner.launchWithRepeatOnLifecycle(
            state = Lifecycle.State.STARTED,
            dispatcher = defaultDispatcher
        ) {
            staticsViewModel.winkCount.collect {
                Log.d("whatisthis", it.toString())

            }
        }

        viewLifecycleOwner.launchWithRepeatOnLifecycle(
            state = Lifecycle.State.STARTED,
            dispatcher = defaultDispatcher
        ) {
            staticsViewModel.drowsyCount.collect {
                Log.d("whatisthis", it.toString())
            }
        }
    }


}