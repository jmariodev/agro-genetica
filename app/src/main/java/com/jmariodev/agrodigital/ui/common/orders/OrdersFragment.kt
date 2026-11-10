package com.jmariodev.agrodigital.ui.common.orders

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.data.model.Order
import com.jmariodev.agrodigital.data.model.OrderStatus
import com.jmariodev.agrodigital.data.model.UserRole
import com.jmariodev.agrodigital.databinding.FragmentOrdersBinding
import com.jmariodev.agrodigital.di.ServiceLocator
import com.jmariodev.agrodigital.ui.common.BindingFragment
import com.jmariodev.agrodigital.ui.common.NavArgs
import com.jmariodev.agrodigital.ui.common.visibleIf

/**
 * Historial de pedidos del comprador o pedidos recibidos por el vendedor (según el rol de la sesión).
 */
class OrdersFragment : BindingFragment<FragmentOrdersBinding>(FragmentOrdersBinding::inflate) {

    private val repository = ServiceLocator.orderRepository
    private var sellerView = false
    private lateinit var adapter: OrderAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = ServiceLocator.currentUser() ?: return
        sellerView = user.role == UserRole.VENDEDOR
        adapter = OrderAdapter(sellerView) { order ->
            findNavController().navigate(R.id.orderDetailFragment, bundleOf(NavArgs.ORDER_ID to order.id))
        }
        binding.rvOrders.adapter = adapter

        with(binding.emptyState) {
            ivEmpty.setImageResource(R.drawable.ic_orders)
            tvEmptyTitle.setText(R.string.orders_empty_title)
            tvEmptyBody.setText(if (sellerView) R.string.orders_empty_body_seller else R.string.orders_empty_body)
        }
        binding.chipActive.setText(if (sellerView) R.string.filter_to_ship else R.string.filter_in_progress)
        binding.chipsStatus.setOnCheckedStateChangeListener { _, _ -> render() }
        render()
    }

    private fun render() {
        val user = ServiceLocator.currentUser() ?: return
        val all = if (sellerView) repository.getBySeller(user.id) else repository.getByBuyer(user.id)
        val filtered = all.filter { it.matches(binding.chipsStatus.checkedChipId) }
        adapter.submitList(filtered)
        val pending = all.count { it.status == OrderStatus.PENDIENTE || it.status == OrderStatus.PAGADO }
        binding.tvSummary.text = resources.getQuantityString(R.plurals.orders_summary, all.size, all.size, pending)
        binding.emptyState.root.visibleIf(filtered.isEmpty())
        binding.rvOrders.visibleIf(filtered.isNotEmpty())
    }

    private fun Order.matches(chipId: Int): Boolean = when (chipId) {
        R.id.chipActive -> status == OrderStatus.PENDIENTE || status == OrderStatus.PAGADO
        R.id.chipShipped -> status == OrderStatus.ENVIADO
        R.id.chipDelivered -> status == OrderStatus.ENTREGADO
        else -> true
    }
}
