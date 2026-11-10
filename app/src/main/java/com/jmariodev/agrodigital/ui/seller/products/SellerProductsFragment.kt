package com.jmariodev.agrodigital.ui.seller.products

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.data.model.Product
import com.jmariodev.agrodigital.data.model.ProductStatus
import com.jmariodev.agrodigital.databinding.FragmentSellerProductsBinding
import com.jmariodev.agrodigital.di.ServiceLocator
import com.jmariodev.agrodigital.ui.common.BindingFragment
import com.jmariodev.agrodigital.ui.common.NavArgs
import com.jmariodev.agrodigital.ui.common.visibleIf

/** CRUD de productos del vendedor (RF-24 a RF-27). */
class SellerProductsFragment : BindingFragment<FragmentSellerProductsBinding>(FragmentSellerProductsBinding::inflate) {

    private val viewModel: SellerProductsViewModel by viewModels {
        viewModelFactory {
            initializer { SellerProductsViewModel(ServiceLocator.productRepository, ServiceLocator.session.userId.orEmpty()) }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = SellerProductAdapter(onClick = ::edit, onMore = ::showActions)
        binding.rvProducts.adapter = adapter

        with(binding.emptyState) {
            ivEmpty.setImageResource(R.drawable.ic_box)
            tvEmptyTitle.setText(R.string.seller_products_empty_title)
            tvEmptyBody.setText(R.string.seller_products_empty_body)
        }

        binding.etSearch.doAfterTextChanged { viewModel.setQuery(it?.toString().orEmpty()) }
        binding.chipsStatus.setOnCheckedStateChangeListener { _, ids ->
            viewModel.setStatus(
                when (ids.firstOrNull()) {
                    R.id.chipApproved -> ProductStatus.APROBADO
                    R.id.chipPending -> ProductStatus.PENDIENTE
                    R.id.chipRejected -> ProductStatus.RECHAZADO
                    else -> null
                }
            )
        }
        binding.fabNew.setOnClickListener { findNavController().navigate(R.id.action_products_to_form) }

        viewModel.products.observe(viewLifecycleOwner) { products ->
            adapter.submitList(products)
            binding.tvCount.text = resources.getQuantityString(R.plurals.products_count, products.size, products.size)
            binding.emptyState.root.visibleIf(products.isEmpty())
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    private fun edit(product: Product) {
        findNavController().navigate(R.id.action_products_to_form, bundleOf(NavArgs.PRODUCT_ID to product.id))
    }

    private fun showActions(product: Product, anchor: View) {
        PopupMenu(requireContext(), anchor).apply {
            menuInflater.inflate(R.menu.menu_product_actions, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.actionEdit -> edit(product)
                    R.id.actionDelete -> confirmDelete(product)
                }
                true
            }
            show()
        }
    }

    private fun confirmDelete(product: Product) {
        MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.ic_delete)
            .setTitle(R.string.delete_product_title)
            .setMessage(getString(R.string.delete_product_message, product.name))
            .setPositiveButton(R.string.action_delete) { _, _ ->
                viewModel.delete(product)
                Snackbar.make(binding.root, getString(R.string.product_deleted, product.name), Snackbar.LENGTH_LONG)
                    .setAnchorView(binding.fabNew)
                    .setAction(R.string.action_undo) { viewModel.restore(product) }
                    .show()
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }
}
