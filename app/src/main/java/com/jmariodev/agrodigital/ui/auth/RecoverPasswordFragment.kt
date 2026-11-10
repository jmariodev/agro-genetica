package com.jmariodev.agrodigital.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.navigation.fragment.findNavController
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.databinding.FragmentAuthRecoverBinding
import com.jmariodev.agrodigital.di.ServiceLocator
import com.jmariodev.agrodigital.ui.common.BindingFragment
import com.jmariodev.agrodigital.ui.common.text

class RecoverPasswordFragment : BindingFragment<FragmentAuthRecoverBinding>(FragmentAuthRecoverBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnBackToLogin.setOnClickListener { findNavController().navigateUp() }
        binding.btnSend.setOnClickListener { sendLink() }
    }

    private fun sendLink() {

        with(binding) {
        val email = tilEmail.text()
        val valid = Patterns.EMAIL_ADDRESS.matcher(email).matches()
        tilEmail.error = if (valid) null else getString(R.string.error_email)
        if (!valid) return
        if (ServiceLocator.authRepository.recoverPassword(email)) {
            tvSuccessBody.text = getString(R.string.recover_success_body, email)
            cardSuccess.visibility = View.VISIBLE
            btnSend.setText(R.string.recover_resend)
        }
    }

    }
}
