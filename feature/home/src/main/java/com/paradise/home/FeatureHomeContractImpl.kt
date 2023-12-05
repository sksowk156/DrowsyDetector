package com.paradise.home

import androidx.core.os.bundleOf
import androidx.navigation.NavController
import com.paradise.common_ui.navigation.FeatureHomeContract

class FeatureHomeContractImpl : FeatureHomeContract {
    override fun show(dataToPass: String, navController: NavController) {
        navController.navigate(R.id.home_navi, bundleOf("argHomeValue" to dataToPass))
    }
}