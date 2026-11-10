package com.jmariodev.agrodigital.ui.common.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.data.model.Order
import com.jmariodev.agrodigital.data.model.OrderStatus
import com.jmariodev.agrodigital.data.model.UserRole
import com.jmariodev.agrodigital.databinding.FragmentOrderDetailBinding
import com.jmariodev.agrodigital.databinding.ItemTimelineStepBinding
import com.jmariodev.agrodigital.di.ServiceLocator
import com.jmariodev.agrodigital.ui.common.BindingFragment
import com.jmariodev.agrodigital.ui.common.NavArgs
import com.jmariodev.agrodigital.ui.common.setOrderStatus
import com.jmariodev.agrodigital.ui.common.snack
import com.jmariodev.agrodigital.ui.common.toCop
import com.jmariodev.agrodigital.ui.common.visibleIf

/**
 * Detalle de pedido. Comprador: seguimiento y cancelación/retracto (Ley 1480).
 * Vendedor: avanzar el estado del despacho.
 */
class OrderDetailFragment : BindingFragment<FragmentOrderDetailBinding>(FragmentOrderDetailBinding::inflate) {

    private val repository = ServiceLocator.orderRepository
    private var sellerView = false
    private lateinit var orderId: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        orderId = requireArguments().getString(NavArgs.ORDER_ID).orEmpty()
        sellerView = ServiceLocator.session.role == UserRole.VENDEDOR
        render()
    }

    private fun render() {
        val order = repository.getById(orderId)
        if (order == null) {
            findNavController().navigateUp()
            return
        }
        with(binding) {
            tvCode.text = order.code
            tvDate.text = getString(R.string.order_date_method, order.date, order.paymentMethod)
            tvStatus.setOrderStatus(order.status)
            containerItems.bindOrderItems(order.items)
            rowSubtotal.set(getString(R.string.summary_subtotal), order.subtotal.toCop())
            rowShipping.set(getString(R.string.summary_shipping), order.shipping.toCop())
            rowTotal.set(getString(R.string.summary_total), order.total.toCop(), emphasis = true)

            rowCounterpart.ivIcon.setImageResource(R.drawable.ic_person)
            rowCounterpart.tvLabel.setText(if (sellerView) R.string.label_buyer else R.string.label_seller)
            rowCounterpart.tvValue.text = if (sellerView) order.buyerName else order.sellerName
            rowAddress.ivIcon.setImageResource(R.drawable.ic_location)
            rowAddress.tvLabel.setText(R.string.checkout_address)
            rowAddress.tvValue.text = order.address
        }
        bindTimeline(order)
        bindActions(order)
    }

    private fun bindTimeline(order: Order) {
        val container = binding.containerTimeline
        container.removeAllViews()
        val steps = listOf(
            Triple(OrderStatus.PAGADO, R.string.status_paid, R.string.timeline_paid),
            Triple(OrderStatus.ENVIADO, R.string.status_shipped, R.string.timeline_shipped),
            Triple(OrderStatus.ENTREGADO, R.string.status_delivered, R.string.timeline_delivered)
        )
        val reached = when (order.status) {
            OrderStatus.PENDIENTE, OrderStatus.CANCELADO -> 0
            OrderStatus.PAGADO -> 1
            OrderStatus.ENVIADO -> 2
            OrderStatus.ENTREGADO -> 3
        }
        steps.forEachIndexed { index, (_, title, detail) ->
            val step = ItemTimelineStepBinding.inflate(LayoutInflater.from(requireContext()), container, true)
            val done = index < reached
            val color = if (done) R.color.agro_primary else R.color.agro_outline
            step.ivDot.backgroundTintList = ContextCompat.getColorStateList(requireContext(), color)
            step.line.setBackgroundColor(ContextCompat.getColor(requireContext(), color))
            step.line.visibleIf(index < steps.lastIndex)
            step.tvStep.setText(title)
            step.tvStepDetail.setText(if (order.status == OrderStatus.CANCELADO && index == 0) R.string.timeline_cancelled else detail)
        }
    }

    private fun bindActions(order: Order) {

        with(binding) {
        val next = when (order.status) {
            OrderStatus.PENDIENTE, OrderStatus.PAGADO -> OrderStatus.ENVIADO
            OrderStatus.ENVIADO -> OrderStatus.ENTREGADO
            else -> null
        }
        btnPrimaryAction.visibleIf(sellerView && next != null)
        if (next != null) {
            btnPrimaryAction.setText(if (next == OrderStatus.ENVIADO) R.string.order_mark_shipped else R.string.order_mark_delivered)
            btnPrimaryAction.setOnClickListener {
                repository.updateStatus(order.id, next)
                snack(R.string.order_status_updated)
                render()
            }
        }
        val cancellable = order.status == OrderStatus.PENDIENTE || order.status == OrderStatus.PAGADO
        btnCancel.visibleIf(!sellerView && cancellable)
        btnCancel.setOnClickListener { confirmCancel(order) }
        bottomBar.visibleIf(btnPrimaryAction.visibility == View.VISIBLE || btnCancel.visibility == View.VISIBLE)
    }

    }

    private fun confirmCancel(order: Order) {
        MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.ic_warning)
            .setTitle(R.string.order_cancel_title)
            .setMessage(getString(R.string.order_cancel_message, order.code, order.total.toCop()))
            .setPositiveButton(R.string.order_cancel_confirm) { _, _ ->
                repository.updateStatus(order.id, OrderStatus.CANCELADO)
                snack(R.string.order_cancelled)
                render()
            }
            .setNegativeButton(R.string.action_back, null)
            .show()
    }
}
