package com.jmariodev.agrodigital.ui.buyer.checkout

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.navigation.fragment.findNavController
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.databinding.FragmentPaymentSuccessBinding
import com.jmariodev.agrodigital.di.ServiceLocator
import com.jmariodev.agrodigital.ui.common.BindingFragment
import com.jmariodev.agrodigital.ui.common.NavArgs
import com.jmariodev.agrodigital.ui.common.RoleHostActivity
import com.jmariodev.agrodigital.ui.common.orders.set
import com.jmariodev.agrodigital.ui.common.setOrderStatus
import com.jmariodev.agrodigital.ui.common.toCop

/** Comprobante de pago exitoso (Ley 527: comprobante digital del pedido). */
class PaymentSuccessFragment : BindingFragment<FragmentPaymentSuccessBinding>(FragmentPaymentSuccessBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val order = ServiceLocator.orderRepository.getById(requireArguments().getString(NavArgs.ORDER_ID).orEmpty())
        if (order != null) {
            binding.tvMessage.text = getString(R.string.payment_success_body, order.code)
            binding.rowOrder.set(getString(R.string.label_order), order.code)
            binding.rowMethod.set(getString(R.string.label_payment_method), order.paymentMethod)
            binding.rowTotal.set(getString(R.string.summary_total), order.total.toCop(), emphasis = true)
            binding.tvStatus.setOrderStatus(order.status)
        }
        binding.successCircle.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_scale_in))
        binding.btnOrders.setOnClickListener { findNavController().navigate(R.id.action_success_to_orders) }
        binding.btnContinue.setOnClickListener {
            (requireActivity() as RoleHostActivity).openTopLevel(R.id.homeFragment)
        }
    }
}
