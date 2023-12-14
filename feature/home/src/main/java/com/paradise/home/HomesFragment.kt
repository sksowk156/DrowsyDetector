package com.paradise.home

import android.Manifest
import android.os.Build
import androidx.navigation.fragment.findNavController
import com.paradise.common.helper.ToastHelper
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

    @Inject
    lateinit var toastHelper: ToastHelper

    override fun onViewCreated() {

        binding.btHomesToanalyze.setOnAvoidDuplicateClick {
            permissionCheckForAnalyzesFragment {
                featureAnalyzeContractImpl.show("def", findNavController())
            }
        }

        binding.btHomesTosetting.setOnAvoidDuplicateClick {
            permissionCheckForSettingFragment {
                featureSettingContractImpl.show("def", findNavController())
            }
        }

        binding.btHomesTostatistic.setOnAvoidDuplicateClick {
            featureStatisticContractImpl.show("abc", findNavController())
        }
    }

    private fun permissionCheckForAnalyzesFragment(changeFragment: () -> Unit) {
        checkPermissions(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.SYSTEM_ALERT_WINDOW
            )
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                checkPermissions(arrayOf(Manifest.permission.FOREGROUND_SERVICE)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        checkPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS)) {
                            changeFragment()
                        }
                    } else {
                        changeFragment()
                    }
                }
            } else {
                changeFragment()
            }
        }
    }

    private fun permissionCheckForSettingFragment(changeFragment: () -> Unit) {
        checkPermissions(
            arrayOf(
                Manifest.permission.RECORD_AUDIO
            )
        ) {
            changeFragment()
        }
    }

}