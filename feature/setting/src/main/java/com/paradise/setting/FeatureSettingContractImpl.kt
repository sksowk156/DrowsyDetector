package com.paradise.setting

import androidx.core.os.bundleOf
import androidx.navigation.NavController
import com.paradise.common_ui.navigation.FeatureSettingContract

class FeatureSettingContractImpl : FeatureSettingContract {
    override fun show(dataToPass: String, navController: NavController) {
        navController.navigate(R.id.setting_navi, bundleOf("argSettingValue" to dataToPass))
    }
}