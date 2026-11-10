package com.jmariodev.agrodigital.ui.admin.users

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.data.model.User
import com.jmariodev.agrodigital.data.model.UserRole
import com.jmariodev.agrodigital.data.model.UserStatus
import com.jmariodev.agrodigital.databinding.FragmentAdminUsersBinding
import com.jmariodev.agrodigital.di.ServiceLocator
import com.jmariodev.agrodigital.ui.common.BindingFragment
import com.jmariodev.agrodigital.ui.common.NavArgs
import com.jmariodev.agrodigital.ui.common.snack
import com.jmariodev.agrodigital.ui.common.visibleIf

/** Gestión de usuarios: listado, filtros, aprobación de vendedores, edición, suspensión y eliminación. */
class AdminUsersFragment : BindingFragment<FragmentAdminUsersBinding>(FragmentAdminUsersBinding::inflate) {

    private val repository = ServiceLocator.userRepository
    private lateinit var pendingAdapter: PendingUserAdapter
    private lateinit var userAdapter: UserAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pendingAdapter = PendingUserAdapter(
            onApprove = { setStatus(it, UserStatus.ACTIVO, R.string.user_approved) },
            onReject = { setStatus(it, UserStatus.INACTIVO, R.string.user_rejected) }
        )
        userAdapter = UserAdapter(onClick = ::edit, onMore = ::showActions)
        binding.rvPending.adapter = pendingAdapter
        binding.rvUsers.adapter = userAdapter

        with(binding.emptyState) {
            ivEmpty.setImageResource(R.drawable.ic_users)
            tvEmptyTitle.setText(R.string.users_empty_title)
            tvEmptyBody.setText(R.string.empty_results_body)
        }

        binding.etSearch.doAfterTextChanged { render() }
        binding.chipsRole.setOnCheckedStateChangeListener { _, _ -> render() }
        binding.fabNewUser.setOnClickListener { findNavController().navigate(R.id.action_users_to_form) }
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    private fun render() {
        val query = binding.etSearch.text?.toString()?.trim()?.lowercase().orEmpty()
        val role = when (binding.chipsRole.checkedChipId) {
            R.id.chipSellers -> UserRole.VENDEDOR
            R.id.chipBuyers -> UserRole.COMPRADOR
            else -> null
        }
        val filtered = repository.getAll().filter { u ->
            (role == null || u.role == role) &&
                (query.isEmpty() || u.name.lowercase().contains(query) || u.email.lowercase().contains(query))
        }
        val pending = filtered.filter { it.status == UserStatus.PENDIENTE }
        val registered = filtered.filter { it.status != UserStatus.PENDIENTE }
        pendingAdapter.submitList(pending)
        userAdapter.submitList(registered)
        binding.tvPendingCount.text = pending.size.toString()
        binding.sectionPending.visibleIf(pending.isNotEmpty())
        binding.tvRegisteredTitle.visibleIf(registered.isNotEmpty())
        binding.emptyState.root.visibleIf(filtered.isEmpty())
    }

    private fun setStatus(user: User, status: UserStatus, message: Int) {
        repository.setStatus(user.id, status)
        snack(getString(message, user.name))
        render()
    }

    private fun edit(user: User) {
        findNavController().navigate(R.id.action_users_to_form, bundleOf(NavArgs.USER_ID to user.id))
    }

    private fun showActions(user: User, anchor: View) {
        PopupMenu(requireContext(), anchor).apply {
            menuInflater.inflate(R.menu.menu_user_actions, menu)
            menu.findItem(R.id.actionToggleStatus).setTitle(
                if (user.status == UserStatus.ACTIVO) R.string.action_suspend else R.string.action_reactivate
            )
            val isSelf = user.id == ServiceLocator.session.userId
            menu.findItem(R.id.actionDelete).isEnabled = !isSelf
            menu.findItem(R.id.actionToggleStatus).isEnabled = !isSelf
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.actionEdit -> edit(user)
                    R.id.actionToggleStatus -> if (user.status == UserStatus.ACTIVO) {
                        setStatus(user, UserStatus.INACTIVO, R.string.user_suspended)
                    } else {
                        setStatus(user, UserStatus.ACTIVO, R.string.user_reactivated)
                    }
                    R.id.actionDelete -> confirmDelete(user)
                }
                true
            }
            show()
        }
    }

    private fun confirmDelete(user: User) {
        MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.ic_delete)
            .setTitle(R.string.delete_user_title)
            .setMessage(getString(R.string.delete_user_message, user.name))
            .setPositiveButton(R.string.action_delete) { _, _ ->
                repository.delete(user.id)
                render()
                Snackbar.make(binding.root, getString(R.string.user_deleted, user.name), Snackbar.LENGTH_LONG)
                    .setAnchorView(binding.fabNewUser)
                    .setAction(R.string.action_undo) {
                        repository.save(user)
                        render()
                    }
                    .show()
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }
}
