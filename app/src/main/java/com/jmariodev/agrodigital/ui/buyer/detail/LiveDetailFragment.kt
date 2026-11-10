package com.jmariodev.agrodigital.ui.buyer.detail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.data.model.Product
import com.jmariodev.agrodigital.databinding.FragmentLiveDetailBinding
import com.jmariodev.agrodigital.databinding.ViewSpecCellBinding
import com.jmariodev.agrodigital.di.ServiceLocator
import com.jmariodev.agrodigital.ui.common.BindingFragment
import com.jmariodev.agrodigital.ui.common.NavArgs
import com.jmariodev.agrodigital.ui.common.snack
import com.jmariodev.agrodigital.ui.common.tintCoat

/** Ejemplar vivo: ficha técnica y contacto directo con el vendedor por llamada o WhatsApp (RF-28). */
class LiveDetailFragment : BindingFragment<FragmentLiveDetailBinding>(FragmentLiveDetailBinding::inflate) {

    private var favorite = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val product = ServiceLocator.productRepository.getById(requireArguments().getString(NavArgs.PRODUCT_ID).orEmpty())
        if (product == null) {
            findNavController().navigateUp()
            return
        }
        bind(product)
        val phone = ServiceLocator.userRepository.getById(product.sellerId)?.phone ?: DEFAULT_PHONE

        binding.btnCall.setOnClickListener { open(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phone.digits()}"))) }
        binding.btnWhatsapp.setOnClickListener { openWhatsApp(phone, product) }
        binding.btnContact.setOnClickListener { openWhatsApp(phone, product) }
        binding.btnFavorite.setOnClickListener {
            favorite = !favorite
            val color = if (favorite) R.color.agro_error else R.color.agro_neutral
            binding.btnFavorite.iconTint = ContextCompat.getColorStateList(requireContext(), color)
            snack(if (favorite) R.string.favorite_added else R.string.favorite_removed)
        }
        binding.docVaccination.root.setOnClickListener { snack(R.string.doc_opening) }
        binding.docBrand.root.setOnClickListener { snack(R.string.doc_opening) }
    }

    private fun bind(product: Product) {

        with(binding) {
        tvName.text = product.name
        tvSubtitle.text = getString(R.string.live_subtitle, product.breed)
        tvDescription.text = product.description
        ivProduct.tintCoat(product.darkCoat)
        sellerCard.tvSellerName.text = product.sellerName
        sellerCard.tvSellerLocation.text = product.location
        specAge.set(R.string.spec_age, getString(R.string.value_years, product.ageYears))
        specWeight.set(R.string.spec_weight, getString(R.string.value_kg, product.weightKg))
        specRegistry.set(R.string.spec_registry, product.icaRegistry)
        specAbv.set(R.string.spec_abv, getString(R.string.value_abv, product.abv))
        docVaccination.tvTitle.setText(R.string.doc_vaccination)
        docBrand.tvTitle.setText(R.string.doc_brand)
    }

    }

    private fun ViewSpecCellBinding.set(label: Int, value: String) {
        tvLabel.setText(label)
        tvValue.text = value
    }

    private fun openWhatsApp(phone: String, product: Product) {
        val message = getString(R.string.whatsapp_message, product.name)
        val uri = Uri.parse("https://wa.me/${phone.digits()}?text=${Uri.encode(message)}")
        open(Intent(Intent.ACTION_VIEW, uri))
    }

    private fun open(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            snack(R.string.error_no_app)
        }
    }

    private fun String.digits() = filter { it.isDigit() }

    private companion object {
        const val DEFAULT_PHONE = "+57 300 123 4567"
    }
}
