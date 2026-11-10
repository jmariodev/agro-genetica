package com.jmariodev.agrodigital.ui.common.profile

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.data.model.OrderStatus
import com.jmariodev.agrodigital.data.model.User
import com.jmariodev.agrodigital.data.model.UserRole
import com.jmariodev.agrodigital.databinding.FragmentProfileBinding
import com.jmariodev.agrodigital.di.ServiceLocator
import com.jmariodev.agrodigital.ui.common.BadgeTone
import com.jmariodev.agrodigital.ui.common.BiometricHelper
import com.jmariodev.agrodigital.ui.common.BindingFragment
import com.jmariodev.agrodigital.ui.common.RoleHostActivity
import com.jmariodev.agrodigital.ui.common.setBadge
import com.jmariodev.agrodigital.ui.common.snack
import com.jmariodev.agrodigital.ui.common.visibleIf

/** Perfil de usuario (comprador, vendedor o administrador). */
class ProfileFragment : BindingFragment<FragmentProfileBinding>(FragmentProfileBinding::inflate) {

    override fun onResume() {
        super.onResume()
        ServiceLocator.currentUser()?.let(::bind)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val session = ServiceLocator.session

        with(binding) {
            rowLocation.ivIcon.setImageResource(R.drawable.ic_location)
            rowLocation.tvLabel.setText(R.string.profile_location)
            rowPhone.ivIcon.setImageResource(R.drawable.ic_phone)
            rowPhone.tvLabel.setText(R.string.label_phone)
            rowEmail.ivIcon.setImageResource(R.drawable.ic_mail)
            rowEmail.tvLabel.setText(R.string.label_email)

            credIca.ivStart.setImageResource(R.drawable.ic_shield)
            credIca.tvTitle.setText(R.string.credential_ica)
            credFedegan.ivStart.setImageResource(R.drawable.ic_shield)
            credFedegan.tvTitle.setText(R.string.credential_fedegan)

            optionOrders.ivIcon.setImageResource(R.drawable.ic_orders)
            optionOrders.tvTitle.setText(R.string.nav_my_orders)
            optionPrivacy.ivIcon.setImageResource(R.drawable.ic_policy)
            optionPrivacy.tvTitle.setText(R.string.profile_privacy)
            optionSettings.ivIcon.setImageResource(R.drawable.ic_settings)
            optionSettings.tvTitle.setText(R.string.profile_settings)

            switchBiometric.isChecked = session.biometricEnabled
            switchBiometric.setOnCheckedChangeListener { _, checked ->
                if (checked && !BiometricHelper.isAvailable(requireContext())) {
                    snack(R.string.biometric_unavailable)
                }
                session.biometricEnabled = checked
            }

            btnEdit.setOnClickListener { findNavController().navigate(R.id.editProfileFragment) }
            optionOrders.root.setOnClickListener { findNavController().navigate(R.id.ordersFragment) }
            optionPrivacy.root.setOnClickListener { showPrivacyPolicy() }
            optionSettings.root.setOnClickListener { snack(R.string.settings_hint) }
            btnLogout.setOnClickListener { (requireActivity() as RoleHostActivity).confirmLogout() }
            credIca.root.setOnClickListener { snack(R.string.doc_opening) }
            credFedegan.root.setOnClickListener { snack(R.string.doc_opening) }
        }
    }

    private fun bind(user: User) {

        with(binding) {
        tvName.text = user.name
        tvFarm.text = user.farm
        rowLocation.tvValue.text = user.location
        rowPhone.tvValue.text = user.phone
        rowEmail.tvValue.text = user.email

        val isSeller = user.role == UserRole.VENDEDOR
        val isBuyer = user.role == UserRole.COMPRADOR
        when (user.role) {
            UserRole.VENDEDOR -> tvRole.setBadge(
                if (user.verified) R.string.badge_verified_seller else R.string.badge_pending_verification,
                if (user.verified) BadgeTone.SUCCESS else BadgeTone.WARNING
            )
            UserRole.COMPRADOR -> tvRole.setBadge(R.string.role_buyer, BadgeTone.INFO)
            UserRole.ADMIN -> tvRole.setBadge(R.string.role_admin, BadgeTone.DARK)
        }
        tvContactTitle.setText(if (isSeller) R.string.profile_farm_data else R.string.profile_contact_data)
        cardStats.visibleIf(isSeller || isBuyer)
        sectionCredentials.visibleIf(isSeller)
        optionOrders.root.visibleIf(isBuyer)

        val orders = ServiceLocator.orderRepository
        if (isSeller) {
            val products = ServiceLocator.productRepository.getBySeller(user.id).size
            val sales = orders.getBySeller(user.id).count { it.status != OrderStatus.CANCELADO }
            statFirst.tvValue.text = products.toString()
            statFirst.tvLabel.setText(R.string.stat_products)
            statSecond.tvValue.text = sales.toString()
            statSecond.tvLabel.setText(R.string.stat_sales)
            statThird.tvValue.text = getString(R.string.rating_value, SELLER_RATING)
            statThird.tvLabel.setText(R.string.stat_rating)
        } else if (isBuyer) {
            val buyerOrders = orders.getByBuyer(user.id)
            statFirst.tvValue.text = buyerOrders.size.toString()
            statFirst.tvLabel.setText(R.string.stat_orders)
            statSecond.tvValue.text = buyerOrders.count { it.status == OrderStatus.ENTREGADO }.toString()
            statSecond.tvLabel.setText(R.string.stat_delivered)
            statThird.tvValue.text = ServiceLocator.cartRepository.count().toString()
            statThird.tvLabel.setText(R.string.stat_in_cart)
        }
    }

    }

    private fun showPrivacyPolicy() {
        MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.ic_policy)
            .setTitle(R.string.privacy_title)
            .setMessage(R.string.privacy_body)
            .setPositiveButton(R.string.action_understood, null)
            .show()
    }

    private companion object {
        const val SELLER_RATING = 4.9f
    }
}
