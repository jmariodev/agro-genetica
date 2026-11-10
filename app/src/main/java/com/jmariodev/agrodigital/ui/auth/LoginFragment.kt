package com.jmariodev.agrodigital.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.navigation.fragment.findNavController
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.data.model.UserRole
import com.jmariodev.agrodigital.databinding.FragmentAuthLoginBinding
import com.jmariodev.agrodigital.di.ServiceLocator
import com.jmariodev.agrodigital.ui.common.BiometricHelper
import com.jmariodev.agrodigital.ui.common.BindingFragment
import com.jmariodev.agrodigital.ui.common.snack
import com.jmariodev.agrodigital.ui.common.text
import com.jmariodev.agrodigital.ui.common.visibleIf

class LoginFragment : BindingFragment<FragmentAuthLoginBinding>(FragmentAuthLoginBinding::inflate) {

    private val authRepository = ServiceLocator.authRepository
    private val session = ServiceLocator.session

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val canUseBiometric = session.biometricEnabled && session.lastUserId != null
        binding.btnBiometric.visibleIf(canUseBiometric)

        binding.btnLogin.setOnClickListener { attemptLogin() }
        binding.btnBiometric.setOnClickListener { loginWithBiometric() }
        binding.btnForgot.setOnClickListener { findNavController().navigate(R.id.action_login_to_recover) }
        binding.btnGoRegister.setOnClickListener { findNavController().navigate(R.id.action_login_to_register) }
        binding.btnGoogle.setOnClickListener { snack(R.string.google_not_available) }

        binding.btnDemoBuyer.setOnClickListener { openRoleHome(authRepository.demoUser(UserRole.COMPRADOR)) }
        binding.btnDemoSeller.setOnClickListener { openRoleHome(authRepository.demoUser(UserRole.VENDEDOR)) }
        binding.btnDemoAdmin.setOnClickListener { openRoleHome(authRepository.demoUser(UserRole.ADMIN)) }
    }

    private fun attemptLogin() {
        val email = binding.tilEmail.text()
        val password = binding.tilPassword.text()
        val emailOk = Patterns.EMAIL_ADDRESS.matcher(email).matches()
        binding.tilEmail.error = if (emailOk) null else getString(R.string.error_email)
        binding.tilPassword.error = if (password.isNotEmpty()) null else getString(R.string.error_required)
        if (!emailOk || password.isEmpty()) return

        val user = authRepository.login(email, password)
        if (user == null) {
            binding.tilPassword.error = getString(R.string.error_credentials)
        } else {
            openRoleHome(user)
        }
    }

    private fun loginWithBiometric() {
        val user = session.lastUserId?.let { ServiceLocator.userRepository.getById(it) } ?: return
        if (!BiometricHelper.isAvailable(requireContext())) {
            snack(R.string.biometric_unavailable)
            return
        }
        BiometricHelper.authenticate(
            requireActivity(),
            title = getString(R.string.biometric_prompt_title),
            subtitle = getString(R.string.biometric_prompt_login),
            onSuccess = { openRoleHome(user) },
            onError = { message -> snack(message) }
        )
    }
}
