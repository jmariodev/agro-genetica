package com.jmariodev.agrodigital.ui.auth

import android.os.Bundle
import android.view.View
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.databinding.FragmentAuthBiometricSetupBinding
import com.jmariodev.agrodigital.di.ServiceLocator
import com.jmariodev.agrodigital.ui.common.BiometricHelper
import com.jmariodev.agrodigital.ui.common.BindingFragment
import com.jmariodev.agrodigital.ui.common.snack

class BiometricSetupFragment :
    BindingFragment<FragmentAuthBiometricSetupBinding>(FragmentAuthBiometricSetupBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.featureInstant) {
            ivIcon.setImageResource(R.drawable.ic_bolt)
            tvTitle.setText(R.string.biometric_feature_fast)
            tvSubtitle.setText(R.string.biometric_feature_fast_body)
        }
        with(binding.featureSecure) {
            ivIcon.setImageResource(R.drawable.ic_shield)
            tvTitle.setText(R.string.biometric_feature_secure)
            tvSubtitle.setText(R.string.biometric_feature_secure_body)
        }
        binding.btnRegister.setOnClickListener { registerFingerprint() }
        binding.btnLater.setOnClickListener { continueToHome() }
        binding.btnClose.setOnClickListener { continueToHome() }
    }

    private fun registerFingerprint() {
        if (!BiometricHelper.isAvailable(requireContext())) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.biometric_unavailable_title)
                .setMessage(R.string.biometric_unavailable_body)
                .setPositiveButton(R.string.action_continue) { _, _ -> continueToHome() }
                .setNegativeButton(R.string.action_cancel, null)
                .show()
            return
        }
        BiometricHelper.authenticate(
            requireActivity(),
            title = getString(R.string.biometric_prompt_title),
            subtitle = getString(R.string.biometric_prompt_register),
            onSuccess = {
                ServiceLocator.session.biometricEnabled = true
                snack(R.string.biometric_enabled)
                continueToHome()
            },
            onError = { message -> snack(message) }
        )
    }

    private fun continueToHome() {
        val user = ServiceLocator.currentUser() ?: return
        openRoleHome(user)
    }
}
