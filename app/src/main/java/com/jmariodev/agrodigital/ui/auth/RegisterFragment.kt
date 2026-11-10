package com.jmariodev.agrodigital.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.navigation.fragment.findNavController
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.data.model.UserRole
import com.jmariodev.agrodigital.databinding.FragmentAuthRegisterBinding
import com.jmariodev.agrodigital.di.ServiceLocator
import com.jmariodev.agrodigital.ui.common.BindingFragment
import com.jmariodev.agrodigital.ui.common.requireText
import com.jmariodev.agrodigital.ui.common.snack
import com.jmariodev.agrodigital.ui.common.text

class RegisterFragment : BindingFragment<FragmentAuthRegisterBinding>(FragmentAuthRegisterBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.dividerOr.tvDividerText.setText(R.string.register_or_form)
        binding.btnCreate.setOnClickListener { attemptRegister() }
        binding.btnGoLogin.setOnClickListener { findNavController().navigateUp() }
        binding.btnGoogle.setOnClickListener { snack(R.string.google_not_available) }
    }

    private fun attemptRegister() {

        with(binding) {
        val nameOk = tilName.requireText(R.string.error_required)
        val email = tilEmail.text()
        val emailOk = Patterns.EMAIL_ADDRESS.matcher(email).matches()
        tilEmail.error = if (emailOk) null else getString(R.string.error_email)
        val phoneOk = tilPhone.text().length >= MIN_PHONE
        tilPhone.error = if (phoneOk) null else getString(R.string.error_phone)
        val farmOk = tilFarm.requireText(R.string.error_required)
        val password = tilPassword.text()
        val passwordOk = password.length >= MIN_PASSWORD && password.any { it.isDigit() }
        tilPassword.error = if (passwordOk) null else getString(R.string.error_password_weak)
        val confirmOk = tilConfirm.text() == password
        tilConfirm.error = if (confirmOk) null else getString(R.string.error_password_match)

        if (!cbTerms.isChecked) {
            snack(R.string.error_terms)
            return
        }
        if (!(nameOk && emailOk && phoneOk && farmOk && passwordOk && confirmOk)) return

        val role = if (toggleRole.checkedButtonId == btnRoleSeller.id) UserRole.VENDEDOR else UserRole.COMPRADOR
        val user = ServiceLocator.authRepository.register(
            name = tilName.text(),
            email = email,
            phone = getString(R.string.phone_prefix) + tilPhone.text(),
            farm = tilFarm.text(),
            role = role,
            password = password
        )
        ServiceLocator.session.saveUser(user)
        findNavController().navigate(R.id.action_register_to_biometric)
    }

    }

    private companion object {
        const val MIN_PHONE = 7
        const val MIN_PASSWORD = 8
    }
}
