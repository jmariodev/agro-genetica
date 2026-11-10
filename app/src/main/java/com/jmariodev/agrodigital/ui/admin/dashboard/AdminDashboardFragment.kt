package com.jmariodev.agrodigital.ui.admin.dashboard

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.data.model.OrderStatus
import com.jmariodev.agrodigital.data.model.ProductStatus
import com.jmariodev.agrodigital.data.model.UserStatus
import com.jmariodev.agrodigital.databinding.FragmentAdminDashboardBinding
import com.jmariodev.agrodigital.databinding.ViewQuickActionBinding
import com.jmariodev.agrodigital.di.ServiceLocator
import com.jmariodev.agrodigital.ui.admin.products.AdminProductsFragment
import com.jmariodev.agrodigital.ui.common.BindingFragment
import com.jmariodev.agrodigital.ui.common.NavArgs
import com.jmariodev.agrodigital.ui.common.RoleHostActivity
import com.jmariodev.agrodigital.ui.common.label
import com.jmariodev.agrodigital.ui.common.set
import com.jmariodev.agrodigital.ui.common.snack
import com.jmariodev.agrodigital.ui.common.toCompactCop
import com.jmariodev.agrodigital.ui.common.visibleIf

/** Dashboard del administrador: métricas globales, aprobaciones pendientes y accesos rápidos. */
class AdminDashboardFragment : BindingFragment<FragmentAdminDashboardBinding>(FragmentAdminDashboardBinding::inflate) {

    private val users = ServiceLocator.userRepository
    private val products = ServiceLocator.productRepository
    private lateinit var adapter: ApprovalAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val admin = ServiceLocator.currentUser()
        binding.header.tvGreeting.text = getString(R.string.dashboard_greeting, admin?.name.orEmpty())
        binding.header.tvSubtitle.setText(R.string.app_name)

        adapter = ApprovalAdapter(onApprove = { resolve(it, approve = true) }, onReject = { resolve(it, approve = false) })
        binding.rvApprovals.adapter = adapter

        binding.qaUsers.setup(R.drawable.ic_users, R.string.qa_users) { open(R.id.usersFragment) }
        binding.qaProducts.setup(R.drawable.ic_box, R.string.qa_products) { open(R.id.adminProductsFragment) }
        binding.qaReports.setup(R.drawable.ic_trending, R.string.qa_reports) { open(R.id.reportsFragment) }
        binding.qaAlerts.setup(R.drawable.ic_warning, R.string.qa_alerts) {
            open(R.id.adminProductsFragment, bundleOf(NavArgs.TAB to AdminProductsFragment.TAB_REPORTED))
        }
        render()
    }

    private fun render() {
        val allUsers = users.getAll()
        val allProducts = products.getAll()
        val pendingUsers = allUsers.filter { it.status == UserStatus.PENDIENTE }
        val pendingProducts = allProducts.filter { it.status == ProductStatus.PENDIENTE }
        val sales = ServiceLocator.orderRepository.getAll().filter { it.status != OrderStatus.CANCELADO }.sumOf { it.total }

        binding.statUsers.set(R.drawable.ic_users, allUsers.size.toString(), getString(R.string.stat_total_users), getString(R.string.trend_users),
            R.color.agro_secondary, R.color.agro_secondary_light)
        binding.statProducts.set(R.drawable.ic_box, allProducts.count { it.status == ProductStatus.APROBADO }.toString(),
            getString(R.string.stat_active_products), getString(R.string.trend_products))
        binding.statSales.set(R.drawable.ic_dollar, sales.toCompactCop(), getString(R.string.stat_month_sales), getString(R.string.trend_sales_admin),
            R.color.agro_tertiary, R.color.agro_primary_light)
        binding.statPending.set(R.drawable.ic_warning, (pendingUsers.size + pendingProducts.size).toString(), getString(R.string.stat_pending),
            null, R.color.agro_warning_dark, R.color.agro_warning_light)

        val items = pendingUsers.map {
            Approval(it.id, it.name, getString(R.string.approval_meta, getString(it.role.label()), it.createdLabel), isProduct = false)
        } + pendingProducts.map {
            Approval(it.id, it.name, getString(R.string.approval_meta, getString(R.string.approval_product), it.createdLabel), isProduct = true)
        }
        adapter.submitList(items)
        binding.tvPendingBadge.text = resources.getQuantityString(R.plurals.new_count, items.size, items.size)
        binding.tvPendingBadge.visibleIf(items.isNotEmpty())
        binding.tvNoPending.visibleIf(items.isEmpty())
    }

    private fun resolve(item: Approval, approve: Boolean) {
        if (item.isProduct) {
            products.setStatus(item.id, if (approve) ProductStatus.APROBADO else ProductStatus.RECHAZADO,
                if (approve) null else getString(R.string.moderation_default_reason))
        } else {
            users.setStatus(item.id, if (approve) UserStatus.ACTIVO else UserStatus.INACTIVO)
        }
        snack(getString(if (approve) R.string.moderation_approved else R.string.moderation_rejected, item.title))
        render()
    }

    private fun open(destination: Int, args: Bundle? = null) {
        (requireActivity() as RoleHostActivity).openTopLevel(destination, args)
    }

    private fun ViewQuickActionBinding.setup(icon: Int, title: Int, onClick: () -> Unit) {
        ivIcon.setImageResource(icon)
        tvTitle.setText(title)
        root.setOnClickListener { onClick() }
    }
}
