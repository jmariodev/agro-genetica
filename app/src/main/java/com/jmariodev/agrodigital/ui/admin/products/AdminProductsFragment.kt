package com.jmariodev.agrodigital.ui.admin.products

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.data.model.Product
import com.jmariodev.agrodigital.data.model.ProductStatus
import com.jmariodev.agrodigital.databinding.DialogTextInputBinding
import com.jmariodev.agrodigital.databinding.FragmentAdminProductsBinding
import com.jmariodev.agrodigital.di.ServiceLocator
import com.jmariodev.agrodigital.ui.common.BindingFragment
import com.jmariodev.agrodigital.ui.common.NavArgs
import com.jmariodev.agrodigital.ui.common.snack
import com.jmariodev.agrodigital.ui.common.text
import com.jmariodev.agrodigital.ui.common.visibleIf

/** Gestión y moderación de productos (RF-21 a RF-23). */
class AdminProductsFragment :
    BindingFragment<FragmentAdminProductsBinding>(FragmentAdminProductsBinding::inflate), ModerationActions {

    private val repository = ServiceLocator.productRepository
    private val adapter = ModerationAdapter(this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvProducts.adapter = adapter
        if (savedInstanceState == null) {
            when (arguments?.getInt(NavArgs.TAB) ?: TAB_PENDING) {
                TAB_APPROVED -> binding.chipsTab.check(R.id.chipApproved)
                TAB_REPORTED -> binding.chipsTab.check(R.id.chipReported)
                else -> binding.chipsTab.check(R.id.chipPending)
            }
        }
        binding.chipsTab.setOnCheckedStateChangeListener { _, _ -> render() }
        binding.etSearch.doAfterTextChanged { render() }
        with(binding.emptyState) {
            ivEmpty.setImageResource(R.drawable.ic_check_circle)
            tvEmptyTitle.setText(R.string.moderation_empty_title)
            tvEmptyBody.setText(R.string.moderation_empty_body)
        }
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    private fun render() {
        val status = when (binding.chipsTab.checkedChipId) {
            R.id.chipApproved -> ProductStatus.APROBADO
            R.id.chipReported -> ProductStatus.REPORTADO
            else -> ProductStatus.PENDIENTE
        }
        val query = binding.etSearch.text?.toString()?.trim()?.lowercase().orEmpty()
        val list = repository.getByStatus(status).filter {
            query.isEmpty() || it.name.lowercase().contains(query) || it.sellerName.lowercase().contains(query)
        }
        adapter.submitList(list)

        val pending = repository.getByStatus(ProductStatus.PENDIENTE).size
        val reported = repository.getByStatus(ProductStatus.REPORTADO).size
        binding.chipPending.text = getString(R.string.filter_pending_count, pending)
        binding.chipReported.text = getString(R.string.filter_reported_count, reported)
        binding.tvSectionTitle.setText(
            when (status) {
                ProductStatus.REPORTADO -> R.string.moderation_reported_title
                ProductStatus.APROBADO -> R.string.moderation_approved_title
                else -> R.string.moderation_for_review
            }
        )
        binding.emptyState.root.visibleIf(list.isEmpty())
        binding.rvProducts.visibleIf(list.isNotEmpty())
    }

    override fun onDetails(product: Product) {
        findNavController().navigate(
            R.id.action_products_to_review,
            bundleOf(NavArgs.PRODUCT_ID to product.id, NavArgs.MODE to NavArgs.MODE_REVIEW)
        )
    }

    override fun onApprove(product: Product) {
        repository.setStatus(product.id, ProductStatus.APROBADO)
        snack(getString(R.string.moderation_approved, product.name))
        render()
    }

    override fun onFlag(product: Product) {
        val dialogBinding = DialogTextInputBinding.inflate(layoutInflater)
        dialogBinding.etInput.setHint(R.string.hint_flag_reason)
        MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.ic_flag)
            .setTitle(R.string.moderation_flag_title)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.action_flag) { _, _ ->
                val reason = dialogBinding.tilInput.text().ifBlank { getString(R.string.moderation_default_reason) }
                repository.setStatus(product.id, ProductStatus.REPORTADO, reason)
                snack(getString(R.string.moderation_flagged, product.name))
                render()
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    override fun onRemove(product: Product) {
        MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.ic_delete)
            .setTitle(R.string.delete_product_title)
            .setMessage(getString(R.string.moderation_remove_message, product.name))
            .setPositiveButton(R.string.moderation_remove) { _, _ ->
                repository.delete(product.id)
                snack(getString(R.string.product_deleted, product.name))
                render()
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    companion object {
        const val TAB_PENDING = 0
        const val TAB_APPROVED = 1
        const val TAB_REPORTED = 2
    }
}
