package com.jmariodev.agrodigital.ui.buyer.checkout

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.data.model.OrderItem
import com.jmariodev.agrodigital.databinding.DialogTextInputBinding
import com.jmariodev.agrodigital.databinding.FragmentBuyerCheckoutBinding
import com.jmariodev.agrodigital.databinding.ViewPaymentOptionBinding
import com.jmariodev.agrodigital.di.ServiceLocator
import com.jmariodev.agrodigital.ui.buyer.cart.CartViewModel
import com.jmariodev.agrodigital.ui.common.BiometricHelper
import com.jmariodev.agrodigital.ui.common.BindingFragment
import com.jmariodev.agrodigital.ui.common.NavArgs
import com.jmariodev.agrodigital.ui.common.orders.bindOrderItems
import com.jmariodev.agrodigital.ui.common.orders.set
import com.jmariodev.agrodigital.ui.common.snack
import com.jmariodev.agrodigital.ui.common.text
import com.jmariodev.agrodigital.ui.common.toCop
import com.jmariodev.agrodigital.ui.common.toast

/**
 * Pasarela de pagos: dirección, resumen, método de pago (API externa tokenizada)
 * y confirmación con huella dactilar (RF-33 a RF-35).
 */
class CheckoutFragment : BindingFragment<FragmentBuyerCheckoutBinding>(FragmentBuyerCheckoutBinding::inflate) {

    private val cartViewModel: CartViewModel by activityViewModels {
        viewModelFactory { initializer { CartViewModel(ServiceLocator.cartRepository) } }
    }

    private lateinit var paymentOptions: List<Pair<ViewPaymentOptionBinding, Int>>
    private var selectedMethod = R.string.pay_mercadopago
    private var addressName = ""
    private var addressLine = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = ServiceLocator.currentUser()
        addressName = user?.farm.orEmpty()
        addressLine = user?.location.orEmpty()
        bindAddress()

        paymentOptions = listOf(
            binding.payMercadoPago to R.string.pay_mercadopago,
            binding.payPse to R.string.pay_pse,
            binding.payEpayco to R.string.pay_epayco
        )
        setupPaymentOption(binding.payMercadoPago, R.string.pay_logo_mp, R.string.pay_mercadopago, R.string.pay_mercadopago_detail, R.color.agro_pay_mercadopago)
        setupPaymentOption(binding.payPse, R.string.pay_logo_pse, R.string.pay_pse, R.string.pay_pse_detail, R.color.agro_pay_pse)
        setupPaymentOption(binding.payEpayco, R.string.pay_logo_ep, R.string.pay_epayco, R.string.pay_epayco_detail, R.color.agro_pay_epayco)
        selectPayment(selectedMethod)

        cartViewModel.items.observe(viewLifecycleOwner) { items ->
            if (items.isEmpty()) return@observe
            val lines = items.map {
                OrderItem(it.product.id, it.product.name, it.product.breed, it.product.type, it.quantity, it.product.price, it.product.darkCoat)
            }
            binding.containerItems.bindOrderItems(lines)
            binding.rowSubtotal.set(getString(R.string.summary_subtotal), cartViewModel.subtotal().toCop())
            binding.rowShipping.set(getString(R.string.summary_shipping), cartViewModel.shipping().toCop())
            binding.rowTotal.set(getString(R.string.summary_total_to_pay), cartViewModel.total().toCop(), emphasis = true)
            binding.btnPay.text = getString(R.string.checkout_pay_amount, cartViewModel.total().toCop())
        }

        binding.btnChangeAddress.setOnClickListener { editAddress() }
        binding.btnPay.setOnClickListener { confirmPayment() }
    }

    private fun bindAddress() {
        binding.tvAddressName.text = addressName
        binding.tvAddressLine.text = addressLine
    }

    private fun setupPaymentOption(option: ViewPaymentOptionBinding, logo: Int, name: Int, detail: Int, color: Int) {
        option.tvLogo.setText(logo)
        option.tvLogo.backgroundTintList = ContextCompat.getColorStateList(requireContext(), color)
        option.tvName.setText(name)
        option.tvDetail.setText(detail)
        option.root.setOnClickListener { selectPayment(name) }
    }

    private fun selectPayment(method: Int) {
        selectedMethod = method
        paymentOptions.forEach { (option, id) ->
            option.root.isChecked = id == method
            option.radio.isChecked = id == method
        }
    }

    private fun editAddress() {
        val dialogBinding = DialogTextInputBinding.inflate(layoutInflater)
        dialogBinding.etInput.setText(addressLine)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.checkout_change_address)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.action_save) { _, _ ->
                val value = dialogBinding.tilInput.text()
                if (value.isNotBlank()) {
                    addressLine = value
                    bindAddress()
                }
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun confirmPayment() {
        if (!binding.cbTerms.isChecked) {
            snack(R.string.error_terms_checkout)
            return
        }
        if (BiometricHelper.isAvailable(requireContext())) {
            BiometricHelper.authenticate(
                requireActivity(),
                title = getString(R.string.biometric_prompt_title),
                subtitle = getString(R.string.biometric_prompt_payment, cartViewModel.total().toCop()),
                onSuccess = { completeOrder() },
                onError = { requireContext().toast(R.string.payment_cancelled) }
            )
        } else {
            // Alternativa por confirmación cuando el dispositivo no tiene huella registrada
            MaterialAlertDialogBuilder(requireContext())
                .setIcon(R.drawable.ic_lock)
                .setTitle(R.string.payment_confirm_title)
                .setMessage(getString(R.string.payment_confirm_message, cartViewModel.total().toCop(), getString(selectedMethod)))
                .setPositiveButton(R.string.payment_confirm_button) { _, _ -> completeOrder() }
                .setNegativeButton(R.string.action_cancel, null)
                .show()
        }
    }

    private fun completeOrder() {
        val user = ServiceLocator.currentUser() ?: return
        val items = cartViewModel.items.value.orEmpty()
        if (items.isEmpty()) return
        val order = ServiceLocator.orderRepository.create(
            buyerId = user.id,
            buyerName = user.name,
            items = items,
            shipping = cartViewModel.shipping(),
            address = "$addressName · $addressLine",
            paymentMethod = getString(selectedMethod)
        )
        cartViewModel.clear()
        findNavController().navigate(R.id.action_checkout_to_success, bundleOf(NavArgs.ORDER_ID to order.id))
    }
}
