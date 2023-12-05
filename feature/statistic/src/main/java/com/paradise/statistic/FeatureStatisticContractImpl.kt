package com.paradise.statistic

import androidx.core.os.bundleOf
import androidx.navigation.NavController
import com.paradise.common_ui.navigation.FeatureStatisticContract

class FeatureStatisticContractImpl : FeatureStatisticContract {
    override fun show(dataToPass: String, navController: NavController) {
        navController.navigate(R.id.statistic_nav, bundleOf("argStatisticalue" to dataToPass))
    }
}