package com.paradise.analyze

import androidx.core.os.bundleOf
import androidx.navigation.NavController
import com.paradise.common_ui.navigation.FeatureAnalyzeContract

class FeatureAnalyzeContractImpl : FeatureAnalyzeContract {
    override fun show(dataToPass: String, navController: NavController) {
        navController.navigate(R.id.analyze_navi, bundleOf("argAnalyzeValue" to dataToPass))
    }
}