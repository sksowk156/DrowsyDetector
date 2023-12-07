package com.paradise.home.navi

import androidx.core.os.bundleOf
import androidx.navigation.NavController
import com.paradise.common_ui.navicontract.FeatureHomeContract
import com.paradise.home.R

class FeatureHomeContractImpl : FeatureHomeContract {
    override fun show(dataToPass: String, navController: NavController) {
        navController.navigate(R.id.home_navi, bundleOf("argHomeValue" to dataToPass))
    }
}