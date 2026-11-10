package com.jmariodev.agrodigital.ui.buyer.cart

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.data.model.CartItem
import com.jmariodev.agrodigital.databinding.FragmentBuyerCartBinding
import com.jmariodev.agrodigital.di.ServiceLocator
import com.jmariodev.agrodigital.ui.common.BindingFragment
import com.jmariodev.agrodigital.ui.common.RoleHostActivity
import com.jmariodev.agrodigital.ui.common.orders.set
import com.jmariodev.agrodigital.ui.common.toCop
import com.jmariodev.agrodigital.ui.common.visibleIf

/** CRUD de carrito: consultar, actualizar cantidades y eliminar ítems (RF-29 a RF-32). */
class CartFragment : BindingFragment<FragmentBuyerCartBinding>(FragmentBuyerCartBinding::inflate) {

    private val viewModel: CartViewModel by activityViewModels {
        viewModelFactory { initializer { CartViewModel(ServiceLocator.cartRepository) } }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = CartAdapter(
            onIncrease = viewModel::increase,
            onDecrease = viewModel::decrease,
            onRemove = ::removeWithUndo
        )
        binding.rvCart.adapter = adapter

        with(binding.emptyState) {
            ivEmpty.setImageResource(R.drawable.ic_cart)
            tvEmptyTitle.setText(R.string.cart_empty_title)
            tvEmptyBody.setText(R.string.cart_empty_body)
            btnEmptyAction.setText(R.string.cart_explore)
            btnEmptyAction.visibility = View.VISIBLE
            btnEmptyAction.setOnClickListener {
                (requireActivity() as RoleHostActivity).openTopLevel(R.id.catalogFragment)
            }
        }

        binding.btnCheckout.setOnClickListener { findNavController().navigate(R.id.action_cart_to_checkout) }

        viewModel.items.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            val empty = items.isEmpty()
            binding.emptyState.root.visibleIf(empty)
            binding.scrollContent.visibleIf(!empty)
            binding.bottomBar.visibleIf(!empty)
            binding.rowSubtotal.set(
                resources.getQuantityString(R.plurals.summary_subtotal_items, viewModel.count(), viewModel.count()),
                viewModel.subtotal().toCop()
            )
            binding.rowShipping.set(getString(R.string.summary_shipping_estimated), viewModel.shipping().toCop())
            binding.rowTotal.set(getString(R.string.summary_total), viewModel.total().toCop(), emphasis = true)
        }
    }

    private fun removeWithUndo(item: CartItem) {
        viewModel.remove(item)
        Snackbar.make(binding.root, getString(R.string.cart_removed, item.product.name), Snackbar.LENGTH_LONG)
            .setAction(R.string.action_undo) { viewModel.restore(item) }
            .show()
    }
}
