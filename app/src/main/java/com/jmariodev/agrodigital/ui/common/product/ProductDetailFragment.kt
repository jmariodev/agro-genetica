package com.jmariodev.agrodigital.ui.common.product

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.data.model.Product
import com.jmariodev.agrodigital.data.model.ProductStatus
import com.jmariodev.agrodigital.data.model.ProductType
import com.jmariodev.agrodigital.databinding.DialogTextInputBinding
import com.jmariodev.agrodigital.databinding.FragmentProductDetailBinding
import com.jmariodev.agrodigital.databinding.ViewSpecCellBinding
import com.jmariodev.agrodigital.di.ServiceLocator
import com.jmariodev.agrodigital.ui.common.BindingFragment
import com.jmariodev.agrodigital.ui.common.NavArgs
import com.jmariodev.agrodigital.ui.common.RoleHostActivity
import com.jmariodev.agrodigital.ui.common.setProductStatus
import com.jmariodev.agrodigital.ui.common.snack
import com.jmariodev.agrodigital.ui.common.text
import com.jmariodev.agrodigital.ui.common.tintCoat
import com.jmariodev.agrodigital.ui.common.toCop
import com.jmariodev.agrodigital.ui.common.visibleIf

/**
 * Detalle de producto. Modo compra (comprador): agregar al carrito.
 * Modo revisión (administrador): aprobar o rechazar la publicación (RF-21).
 */
class ProductDetailFragment : BindingFragment<FragmentProductDetailBinding>(FragmentProductDetailBinding::inflate) {

    private val repository = ServiceLocator.productRepository
    private var favorite = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val product = repository.getById(requireArguments().getString(NavArgs.PRODUCT_ID).orEmpty())
        if (product == null) {
            findNavController().navigateUp()
            return
        }
        val reviewMode = arguments?.getString(NavArgs.MODE) == NavArgs.MODE_REVIEW
        bindProduct(product, reviewMode)

        binding.btnFavorite.setOnClickListener { toggleFavorite() }
        binding.btnShare.setOnClickListener { share(product) }
        binding.docSanitary.root.setOnClickListener { snack(R.string.doc_opening) }
        binding.docRegistry.root.setOnClickListener { snack(R.string.doc_opening) }

        binding.btnAddToCart.setOnClickListener {
            ServiceLocator.cartRepository.add(product)
            snack(getString(R.string.cart_added, product.name))
            (requireActivity() as? RoleHostActivity)?.openTopLevel(R.id.cartFragment)
        }
        binding.btnApprove.setOnClickListener {
            repository.setStatus(product.id, ProductStatus.APROBADO)
            snack(getString(R.string.moderation_approved, product.name))
            findNavController().navigateUp()
        }
        binding.btnReject.setOnClickListener { askRejectReason(product) }
    }

    private fun bindProduct(product: Product, reviewMode: Boolean) {

        with(binding) {
        tvName.text = product.name
        tvPrice.text = if (product.isLiveAnimal) getString(R.string.price_negotiable) else product.price.toCop()
        tvMeta.text = getString(R.string.detail_meta, product.breed, product.abv)
        tvDescription.text = product.description
        tvDescription.visibleIf(product.description.isNotBlank())
        tvPremium.visibleIf(product.premium)
        tvCertified.visibleIf(product.hasCertificate)
        ivProduct.tintCoat(product.darkCoat)

        tvTreeRoot.text = product.name
        tvFather.text = getString(R.string.pedigree_father, product.father)
        tvMother.text = getString(R.string.pedigree_mother, product.mother)

        docSanitary.tvTitle.setText(R.string.doc_sanitary)
        docRegistry.tvTitle.setText(R.string.doc_purity)
        if (!product.hasCertificate) {
            docSanitary.tvTitle.setText(R.string.doc_missing)
            docSanitary.ivStart.setImageResource(R.drawable.ic_warning)
            docSanitary.ivStart.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.agro_error)
        }

        when (product.type) {
            ProductType.EJEMPLAR -> {
                spec1.set(R.string.spec_age, getString(R.string.value_years, product.ageYears))
                spec2.set(R.string.spec_weight, getString(R.string.value_kg, product.weightKg))
                spec3.set(R.string.spec_registry, product.icaRegistry)
                spec4.set(R.string.spec_abv, getString(R.string.value_abv, product.abv))
            }
            ProductType.EMBRION -> {
                spec1.set(R.string.spec_genotype, product.genotype)
                spec2.set(R.string.spec_grade, product.concentration)
                spec3.set(R.string.spec_stage, product.dose)
                spec4.set(R.string.spec_stock, getString(R.string.value_units, product.stock))
            }
            ProductType.PAJILLA -> {
                spec1.set(R.string.spec_genotype, product.genotype)
                spec2.set(R.string.spec_concentration, product.concentration)
                spec3.set(R.string.spec_dose, product.dose)
                spec4.set(R.string.spec_stock, getString(R.string.value_units, product.stock))
            }
        }

        sellerCard.tvSellerName.text = product.sellerName
        sellerCard.tvSellerLocation.text = product.location

        // Modo revisión del administrador
        tvStatus.visibleIf(reviewMode)
        tvStatus.setProductStatus(product.status)
        tvReportReason.visibleIf(reviewMode && product.reportReason != null)
        tvReportReason.text = getString(R.string.moderation_reason, product.reportReason.orEmpty())
        btnAddToCart.visibleIf(!reviewMode && !product.isLiveAnimal)
        btnApprove.visibleIf(reviewMode && product.status != ProductStatus.APROBADO)
        btnReject.visibleIf(reviewMode && product.status != ProductStatus.RECHAZADO)
        bottomBar.visibleIf(btnAddToCart.visibility == View.VISIBLE || reviewMode)
    }

    }

    private fun ViewSpecCellBinding.set(label: Int, value: String) {
        tvLabel.setText(label)
        tvValue.text = value
    }

    private fun toggleFavorite() {
        favorite = !favorite
        val color = if (favorite) R.color.agro_error else R.color.agro_neutral
        binding.btnFavorite.iconTint = ContextCompat.getColorStateList(requireContext(), color)
        snack(if (favorite) R.string.favorite_added else R.string.favorite_removed)
    }

    private fun share(product: Product) {
        val text = getString(R.string.share_text, product.name, product.breed, product.price.toCop())
        val intent = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(intent, getString(R.string.cd_share)))
    }

    private fun askRejectReason(product: Product) {
        val dialogBinding = DialogTextInputBinding.inflate(layoutInflater)
        dialogBinding.etInput.setHint(R.string.hint_reject_reason)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.moderation_reject_title)
            .setMessage(getString(R.string.moderation_reject_message, product.name))
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.action_reject) { _, _ ->
                val reason = dialogBinding.tilInput.text().ifBlank { getString(R.string.moderation_default_reason) }
                repository.setStatus(product.id, ProductStatus.RECHAZADO, reason)
                snack(getString(R.string.moderation_rejected, product.name))
                findNavController().navigateUp()
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }
}
