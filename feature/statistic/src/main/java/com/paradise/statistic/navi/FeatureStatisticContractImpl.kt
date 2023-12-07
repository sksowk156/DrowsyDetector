package com.paradise.statistic.navi

import androidx.core.os.bundleOf
import androidx.navigation.NavController
import com.paradise.common_ui.navicontract.FeatureStatisticContract
import com.paradise.statistic.R

class FeatureStatisticContractImpl : FeatureStatisticContract {
    override fun show(dataToPass: String, navController: NavController) {
        navController.navigate(R.id.statistic_nav, bundleOf("argStatisticalue" to dataToPass))
    }
}