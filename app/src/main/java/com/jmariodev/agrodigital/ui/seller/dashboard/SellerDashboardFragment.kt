package com.jmariodev.agrodigital.ui.seller.dashboard

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.data.model.OrderStatus
import com.jmariodev.agrodigital.data.model.ProductStatus
import com.jmariodev.agrodigital.data.model.ProductType
import com.jmariodev.agrodigital.databinding.FragmentSellerDashboardBinding
import com.jmariodev.agrodigital.di.ServiceLocator
import com.jmariodev.agrodigital.ui.common.BindingFragment
import com.jmariodev.agrodigital.ui.common.NavArgs
import com.jmariodev.agrodigital.ui.common.RoleHostActivity
import com.jmariodev.agrodigital.ui.common.orders.OrderAdapter
import com.jmariodev.agrodigital.ui.common.set
import com.jmariodev.agrodigital.ui.common.toCompactCop

/** Panel del vendedor: métricas, pedidos recientes y acciones rápidas. */
class SellerDashboardFragment : BindingFragment<FragmentSellerDashboardBinding>(FragmentSellerDashboardBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = ServiceLocator.currentUser() ?: return
        binding.header.tvGreeting.text = getString(R.string.dashboard_greeting, user.name.substringBefore(' '))
        binding.header.tvSubtitle.text = user.farm

        val orders = ServiceLocator.orderRepository.getBySeller(user.id)
        val products = ServiceLocator.productRepository.getBySeller(user.id)
        val sales = orders.filter { it.status != OrderStatus.CANCELADO }.sumOf { it.total }

        binding.statSales.set(R.drawable.ic_dollar, sales.toCompactCop(), getString(R.string.stat_total_sales), getString(R.string.trend_sales))
        binding.statProducts.set(
            R.drawable.ic_box, products.count { it.status == ProductStatus.APROBADO }.toString(),
            getString(R.string.stat_active_products), getString(R.string.trend_products),
            R.color.agro_secondary, R.color.agro_secondary_light
        )
        binding.statVisits.set(R.drawable.ic_eye, getString(R.string.value_visits), getString(R.string.stat_profile_visits), getString(R.string.trend_visits))
        binding.statOrders.set(
            R.drawable.ic_trending, orders.size.toString(), getString(R.string.stat_month_orders), getString(R.string.trend_orders),
            R.color.agro_warning_dark, R.color.agro_warning_light
        )

        val adapter = OrderAdapter(sellerView = true) { order ->
            findNavController().navigate(R.id.orderDetailFragment, bundleOf(NavArgs.ORDER_ID to order.id))
        }
        binding.rvRecentOrders.adapter = adapter
        adapter.submitList(orders.take(RECENT_ORDERS))

        binding.btnSeeOrders.setOnClickListener { (requireActivity() as RoleHostActivity).openTopLevel(R.id.ordersFragment) }
        binding.btnNewSemen.setOnClickListener { newProduct(ProductType.PAJILLA) }
        binding.btnNewLive.setOnClickListener { newProduct(ProductType.EJEMPLAR) }
        binding.fabAdd.setOnClickListener { newProduct(null) }
    }

    private fun newProduct(type: ProductType?) {
        findNavController().navigate(R.id.action_dashboard_to_form, bundleOf(NavArgs.TYPE to type?.name))
    }

    private companion object {
        const val RECENT_ORDERS = 3
    }
}
