package com.jmariodev.agrodigital.ui.admin.users

import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.data.model.User
import com.jmariodev.agrodigital.data.model.UserRole
import com.jmariodev.agrodigital.data.model.UserStatus
import com.jmariodev.agrodigital.databinding.FragmentUserFormBinding
import com.jmariodev.agrodigital.di.ServiceLocator
import com.jmariodev.agrodigital.ui.common.BindingFragment
import com.jmariodev.agrodigital.ui.common.NavArgs
import com.jmariodev.agrodigital.ui.common.label
import com.jmariodev.agrodigital.ui.common.requireText
import com.jmariodev.agrodigital.ui.common.snack
import com.jmariodev.agrodigital.ui.common.text
import com.jmariodev.agrodigital.ui.common.visibleIf

/** Alta manual o edición de un usuario por parte del administrador (RF-13, RF-15). */
class UserFormFragment : BindingFragment<FragmentUserFormBinding>(FragmentUserFormBinding::inflate) {

    private val repository = ServiceLocator.userRepository
    private var editing: User? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editing = arguments?.getString(NavArgs.USER_ID)?.let { repository.getById(it) }
        val roles = UserRole.entries
        binding.actRole.setText(getString(UserRole.COMPRADOR.label()), false)

        editing?.let { user ->
            (requireActivity() as AppCompatActivity).supportActionBar?.setTitle(R.string.user_form_edit)
            binding.tilName.editText?.setText(user.name)
            binding.tilEmail.editText?.setText(user.email)
            binding.tilPhone.editText?.setText(user.phone)
            binding.tilFarm.editText?.setText(user.farm)
            binding.tilLocation.editText?.setText(user.location)
            binding.actRole.setText(getString(user.role.label()), false)
            binding.switchActive.isChecked = user.status != UserStatus.INACTIVO
            binding.groupPassword.visibleIf(false)
            binding.btnSave.setText(R.string.action_save_changes)
            binding.btnDelete.visibleIf(user.id != ServiceLocator.session.userId)
        }

        binding.btnSave.setOnClickListener { save(roles) }
        binding.btnDelete.setOnClickListener { editing?.let(::confirmDelete) }
    }

    private fun save(roles: List<UserRole>) {
        val nameOk = binding.tilName.requireText(R.string.error_required)
        val email = binding.tilEmail.text()
        val emailOk = Patterns.EMAIL_ADDRESS.matcher(email).matches()
        binding.tilEmail.error = if (emailOk) null else getString(R.string.error_email)
        val duplicated = repository.getByEmail(email)?.let { it.id != editing?.id } == true
        if (duplicated) binding.tilEmail.error = getString(R.string.error_email_taken)
        val phoneOk = binding.tilPhone.requireText(R.string.error_required)
        val passwordOk = editing != null || binding.tilPassword.text().length >= MIN_PASSWORD
        binding.tilPassword.error = if (passwordOk) null else getString(R.string.error_password_weak)
        if (!nameOk || !emailOk || duplicated || !phoneOk || !passwordOk) return

        val roleText = binding.actRole.text.toString()
        val role = roles.firstOrNull { getString(it.label()) == roleText } ?: UserRole.COMPRADOR
        val status = if (binding.switchActive.isChecked) UserStatus.ACTIVO else UserStatus.INACTIVO
        val user = User(
            id = editing?.id ?: repository.newId(),
            name = binding.tilName.text(),
            email = email,
            phone = binding.tilPhone.text(),
            role = role,
            status = status,
            farm = binding.tilFarm.text(),
            location = binding.tilLocation.text(),
            verified = editing?.verified ?: (role != UserRole.VENDEDOR),
            createdLabel = editing?.createdLabel ?: getString(R.string.label_today)
        )
        repository.save(user)
        snack(getString(if (editing == null) R.string.user_created else R.string.user_updated, user.name))
        findNavController().navigateUp()
    }

    private fun confirmDelete(user: User) {
        MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.ic_delete)
            .setTitle(R.string.delete_user_title)
            .setMessage(getString(R.string.delete_user_message, user.name))
            .setPositiveButton(R.string.action_delete) { _, _ ->
                repository.delete(user.id)
                snack(getString(R.string.user_deleted, user.name))
                findNavController().navigateUp()
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private companion object {
        const val MIN_PASSWORD = 8
    }
}
