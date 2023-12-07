package com.paradise.home

import androidx.navigation.fragment.findNavController
import com.paradise.common.helper.MusicHelper
import com.paradise.common.helper.ToastHelper
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

    @Inject
    lateinit var musicHelper: MusicHelper

    @Inject
    lateinit var toastHelper: ToastHelper
    override fun onViewCreated() {
        with(binding) {
            text.setOnClickListener {
                featureStatisticContractImpl.show("abc", findNavController())
            }
            text2.setOnClickListener {
                featureSettingContractImpl.show("def", findNavController())
            }
            text3.setOnClickListener {
                featureAnalyzeContractImpl.show("def", findNavController())

            }
        }
        musicHelper.initMusicHelper()
        toastHelper.showToast(musicHelper.isPrepared.value.toString())
    }

}