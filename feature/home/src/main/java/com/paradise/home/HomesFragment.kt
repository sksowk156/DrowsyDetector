package com.paradise.home

import android.Manifest
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.paradise.common.helper.method.checkPermissions
import com.paradise.common_ui.base.BaseFragment
import com.paradise.common_ui.navicontract.FeatureAnalyzeContract
import com.paradise.common_ui.navicontract.FeatureSettingContract
import com.paradise.common_ui.navicontract.FeatureStatisticContract
import com.paradise.home.databinding.FragmentHomesBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.WithFragmentBindings
import javax.inject.Inject

@WithFragmentBindings
@AndroidEntryPoint
class HomesFragment : BaseFragment<FragmentHomesBinding>(FragmentHomesBinding::inflate) {
    @Inject
    lateinit var featureStatisticContractImpl: FeatureStatisticContract

    @Inject
    lateinit var featureSettingContractImpl: FeatureSettingContract

    @Inject
    lateinit var featureAnalyzeContractImpl: FeatureAnalyzeContract


    override fun onViewCreated() {
        binding.btHomesToanalyze.setOnAvoidDuplicateClick {
            checkPermissions(arrayOf(Manifest.permission.CAMERA)) {
                featureAnalyzeContractImpl.show("def", findNavController())
            }
        }

        binding.btHomesTosetting.setOnAvoidDuplicateClick {
            featureSettingContractImpl.show("def", findNavController())
        }

        binding.btHomesTostatistic.setOnAvoidDuplicateClick {
            featureStatisticContractImpl.show("abc", findNavController())
        }
    }

}