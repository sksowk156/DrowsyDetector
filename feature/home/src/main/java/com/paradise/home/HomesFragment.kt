package com.paradise.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.paradise.common_ui.navigation.BaseFragment
import com.paradise.common_ui.navigation.FeatureAnalyzeContract
import com.paradise.common_ui.navigation.FeatureSettingContract
import com.paradise.common_ui.navigation.FeatureStatisticContract
import com.paradise.home.databinding.FragmentHomesBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomesFragment : BaseFragment<FragmentHomesBinding>(FragmentHomesBinding::inflate) {
    @Inject
    lateinit var featureStatisticContractImpl: FeatureStatisticContract

    @Inject
    lateinit var featureSettingContractImpl: FeatureSettingContract

    @Inject
    lateinit var featureAnalyzeContractImpl: FeatureAnalyzeContract

    override fun onViewCreated() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding){
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


    }

}