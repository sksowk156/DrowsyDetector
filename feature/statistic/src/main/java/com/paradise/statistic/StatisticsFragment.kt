package com.paradise.statistic

import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.core.model.drowsyResultItem
import com.core.model.winkResultItem
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.paradise.common.network.defaultDispatcher
import com.paradise.common.network.getTodayDate
import com.paradise.common.utils.launchWithRepeatOnLifecycle
import com.paradise.common_ui.base.BaseFragment
import com.paradise.common_ui.R
import com.paradise.statistic.databinding.FragmentStatisticsBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.WithFragmentBindings
import kotlinx.coroutines.launch

@WithFragmentBindings
@AndroidEntryPoint
class StatisticsFragment :
    BaseFragment<FragmentStatisticsBinding>(FragmentStatisticsBinding::inflate) {
    private val staticsViewModel: StatisticsViewModel by viewModels()

    override fun onViewCreated() {
        binding.toolbarStatistic.setToolbarMenu("통계", true)
        subscribeTodayRecord()
        staticsViewModel.getRecord(getTodayDate())
    }


    private fun subscribeTodayRecord() {
        viewLifecycleOwner.launchWithRepeatOnLifecycle(
            state = Lifecycle.State.STARTED, dispatcher = defaultDispatcher
        ) {
            staticsViewModel.analyzeRecord.collect {
                if (it != null) {
                    staticsViewModel.getAnalyzeResult(it.id)
                }
            }

        }

        viewLifecycleOwner.launchWithRepeatOnLifecycle(
            state = Lifecycle.State.STARTED, dispatcher = defaultDispatcher
        ) {
            staticsViewModel.allAnayzeResult.collect {
                if (it.first !=null && it.second!=null) {
                    binding.tvStatisticDrowsycount.text = it.second!!.size.toString()
                    viewLifecycleOwner.lifecycleScope.launch { // 차트를 그려야 하므로 mainScope에서 해야 된다.
                        initChart(it.first!!, it.second!!)
                    }
                }
            }
        }
    }

    private fun initChart(winkList: List<winkResultItem>, drowsyList: List<drowsyResultItem>) {
        with(binding) {
            // Bar 데이터 생성
            val barEntries = ArrayList<BarEntry>()
            for (i in winkList.indices) {
                barEntries.add(
                    BarEntry(
                        (i + 1).toFloat(), winkList[i].value.toFloat()
                    )
                ) // 1(간격)부터 시작
            }
            val barDataSet = BarDataSet(barEntries, "눈 깜빡임 횟수")
            barDataSet.color = ContextCompat.getColor(requireContext(), R.color.primary1)
            barDataSet.axisDependency = YAxis.AxisDependency.LEFT
            barDataSet.valueTextSize = 10f
            // BarData 생성
            val barData = BarData(barDataSet)
            barData.barWidth = 0.8f


            // Line 데이터 생성
            val lineEntries = ArrayList<Entry>()
            for (i in drowsyList.indices) {
                lineEntries.add(Entry((i + 1).toFloat(), drowsyList[i].value.toFloat()))
            }
            val lineDataSet = LineDataSet(lineEntries, "졸음 횟수")
            lineDataSet.color = Color.YELLOW
            lineDataSet.axisDependency = YAxis.AxisDependency.RIGHT
            lineDataSet.setDrawCircles(true)
            lineDataSet.valueTextSize = 10f
            lineDataSet.lineWidth = 3f
            // BarData 생성
            val lineData = LineData(lineDataSet)


            // X 축 설정
            val xAxis: XAxis = chartStatisticAnalyzeresult.xAxis
            xAxis.position = XAxis.XAxisPosition.BOTH_SIDED
            xAxis.axisMinimum = 0f
            xAxis.granularity = 1f
            xAxis.valueFormatter = CustomXAxisFormatter()
            xAxis.setDrawGridLines(false)
            xAxis.position = XAxis.XAxisPosition.BOTTOM

            // 왼쪽 Y 축 설정 (눈깜빡임)
            val leftAxis: YAxis = chartStatisticAnalyzeresult.axisLeft
            leftAxis.setDrawGridLines(false)
            // 오른쪽 Y 축 설정 (졸음)
            val rightAxis: YAxis = chartStatisticAnalyzeresult.axisRight
            rightAxis.setDrawGridLines(false)
            rightAxis.granularity = (rightAxis.granularity.toInt()).toFloat()// 정수 단위로 출력되게끔

            // CombinedChart에 데이터 설정
            val combinedData = CombinedData()
            combinedData.setData(lineData)
            combinedData.setData(barData)

            chartStatisticAnalyzeresult.data = combinedData
            // 그래프 스타일 및 기타 설정
            chartStatisticAnalyzeresult.description.isEnabled = false
            chartStatisticAnalyzeresult.legend.isEnabled = true
            chartStatisticAnalyzeresult.setBackgroundColor(Color.WHITE)
            chartStatisticAnalyzeresult.setDrawGridBackground(false)
            chartStatisticAnalyzeresult.setDrawBarShadow(false)
            chartStatisticAnalyzeresult.isHighlightFullBarEnabled = false
            chartStatisticAnalyzeresult.setVisibleXRange(0f, (winkList.size + 1).toFloat()) // 총 너비

            chartStatisticAnalyzeresult.animateXY(1500, 1500)
            chartStatisticAnalyzeresult.invalidate()
        }
    }

    // X 축 값 포맷터 클래스
    inner class CustomXAxisFormatter : ValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            // X 축 값 포맷을 원하는 대로 설정
            return "${(value).toInt()}분"
        }
    }
}
