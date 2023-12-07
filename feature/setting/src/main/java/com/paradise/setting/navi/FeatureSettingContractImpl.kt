package com.paradise.setting.navi

import androidx.core.os.bundleOf
import androidx.navigation.NavController
import com.paradise.common_ui.navicontract.FeatureSettingContract
import com.paradise.setting.R

class FeatureSettingContractImpl : FeatureSettingContract {
    override fun show(dataToPass: String, navController: NavController) {
        navController.navigate(R.id.setting_navi, bundleOf("argSettingValue" to dataToPass))
    }
}