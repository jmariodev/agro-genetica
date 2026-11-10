package com.jmariodev.agrodigital.ui.seller.products

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.data.model.Product
import com.jmariodev.agrodigital.data.model.ProductStatus
import com.jmariodev.agrodigital.data.model.ProductType
import com.jmariodev.agrodigital.databinding.FragmentProductFormBinding
import com.jmariodev.agrodigital.di.ServiceLocator
import com.jmariodev.agrodigital.ui.common.BindingFragment
import com.jmariodev.agrodigital.ui.common.NavArgs
import com.jmariodev.agrodigital.ui.common.requireText
import com.jmariodev.agrodigital.ui.common.snack
import com.jmariodev.agrodigital.ui.common.text
import com.jmariodev.agrodigital.ui.common.visibleIf
import java.util.Locale

/**
 * Crear o editar un producto. Usa la cámara nativa, el GPS del dispositivo y adjunta el
 * certificado sanitario ICA en PDF. Todo producto nuevo queda PENDIENTE de aprobación.
 */
class ProductFormFragment : BindingFragment<FragmentProductFormBinding>(FragmentProductFormBinding::inflate) {

    private val repository = ServiceLocator.productRepository
    private var editing: Product? = null
    private var pdfName: String? = null
    private var location: String? = null

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            binding.ivPreview.setImageBitmap(bitmap)
            showPreview()
        }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            binding.ivPreview.setImageURI(uri)
            showPreview()
        }
    }

    private val pickPdf = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            pdfName = displayName(uri)
            binding.tvPdfName.text = pdfName
            binding.tvPdfName.visibility = View.VISIBLE
            binding.btnAttachPdf.setText(R.string.form_replace_pdf)
        }
    }

    private val locationPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result.values.any { it }) readLocation() else snack(R.string.location_denied)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editing = arguments?.getString(NavArgs.PRODUCT_ID)?.let { repository.getById(it) }

        binding.toggleType.addOnButtonCheckedListener { _, _, _ -> updateTypeFields() }
        arguments?.getString(NavArgs.TYPE)?.let { name ->
            runCatching { ProductType.valueOf(name) }.getOrNull()?.let { checkType(it) }
        }
        editing?.let { fill(it) }
        updateTypeFields()

        binding.btnCamera.setOnClickListener { takePicture.launch(null) }
        binding.btnRetakePhoto.setOnClickListener { takePicture.launch(null) }
        binding.btnGallery.setOnClickListener { pickImage.launch(IMAGE_MIME) }
        binding.btnGps.setOnClickListener { requestLocation() }
        binding.btnAttachPdf.setOnClickListener { pickPdf.launch(arrayOf(PDF_MIME)) }
        binding.btnPublish.setOnClickListener { save() }
    }

    private fun fill(product: Product) {

        with(binding) {
        (requireActivity() as AppCompatActivity).supportActionBar?.setTitle(R.string.form_title_edit)
        btnPublish.setText(R.string.action_save_changes)
        checkType(product.type)
        tilName.editText?.setText(product.name)
        actBreed.setText(product.breed, false)
        tilIca.editText?.setText(product.icaRegistry)
        tilAbv.editText?.setText(product.abv.toString())
        tilStock.editText?.setText(product.stock.toString())
        tilPrice.editText?.setText(if (product.price > 0) product.price.toString() else "")
        tilAge.editText?.setText(product.ageYears.takeIf { it > 0 }?.toString().orEmpty())
        tilWeight.editText?.setText(product.weightKg.takeIf { it > 0 }?.toString().orEmpty())
        tilDescription.editText?.setText(product.description)
        location = product.location
        tvLocation.text = getString(R.string.location_coords, product.location)
        if (product.hasCertificate) {
            pdfName = getString(R.string.form_existing_pdf, product.icaRegistry)
            tvPdfName.text = pdfName
            tvPdfName.visibility = View.VISIBLE
        }
    }

    }

    private fun checkType(type: ProductType) {
        binding.toggleType.check(
            when (type) {
                ProductType.PAJILLA -> R.id.btnTypeSemen
                ProductType.EMBRION -> R.id.btnTypeEmbryo
                ProductType.EJEMPLAR -> R.id.btnTypeLive
            }
        )
    }

    private fun selectedType(): ProductType = when (binding.toggleType.checkedButtonId) {
        R.id.btnTypeEmbryo -> ProductType.EMBRION
        R.id.btnTypeLive -> ProductType.EJEMPLAR
        else -> ProductType.PAJILLA
    }

    /** Los ejemplares vivos piden edad y peso; su precio es opcional (se negocia). */
    private fun updateTypeFields() {
        with(binding) {
        val live = selectedType() == ProductType.EJEMPLAR
        groupLive.visibleIf(live)
        tvNameLabel.setText(if (live) R.string.label_animal_name else R.string.label_product_name)
        tvStockLabel.setText(if (live) R.string.label_units else R.string.label_stock)
        tvPriceLabel.setText(if (live) R.string.label_price_optional else R.string.label_price)
    }
    }

    private fun showPreview() {
        binding.ivPreview.visibility = View.VISIBLE
        binding.btnRetakePhoto.visibility = View.VISIBLE
        binding.photoPlaceholder.visibility = View.INVISIBLE
    }

    private fun requestLocation() {
        val granted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) readLocation() else locationPermission.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    /** Lectura única (getLastKnownLocation) para minimizar el consumo de batería (RNF de GPS). */
    @SuppressLint("MissingPermission")
    private fun readLocation() {
        val manager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val last = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { manager.isProviderEnabled(it) }
            .firstNotNullOfOrNull { manager.getLastKnownLocation(it) }
        val (lat, lng) = if (last != null) last.latitude to last.longitude else DEFAULT_LAT to DEFAULT_LNG
        location = String.format(Locale.US, "%.4f, %.4f", lat, lng)
        binding.tvLocation.text = getString(R.string.location_coords, location)
        binding.btnGps.setText(R.string.form_gps_update)
        snack(if (last != null) R.string.location_updated else R.string.location_default)
    }

    private fun displayName(uri: Uri): String {
        requireContext().contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) return cursor.getString(0)
        }
        return getString(R.string.form_pdf_default_name)
    }

    private fun save() {
        with(binding) {
        val type = selectedType()
        val live = type == ProductType.EJEMPLAR
        val nameOk = tilName.requireText(R.string.error_required)
        val breed = actBreed.text.toString()
        tilBreed.error = if (breed.isBlank()) getString(R.string.error_required) else null
        val icaOk = tilIca.requireText(R.string.error_required)
        val stock = tilStock.text().toIntOrNull()
        tilStock.error = if (stock == null || stock < 1) getString(R.string.error_min_one) else null
        val price = tilPrice.text().toLongOrNull() ?: 0L
        tilPrice.error = if (!live && price <= 0) getString(R.string.error_price) else null
        val certificateOk = live || pdfName != null
        if (!certificateOk) snack(R.string.error_certificate)

        if (!nameOk || breed.isBlank() || !icaOk || stock == null || stock < 1 || (!live && price <= 0) || !certificateOk) {
            return
        }

        val seller = ServiceLocator.currentUser() ?: return
        val current = editing
        val ica = tilIca.text()
        val product = Product(
            id = current?.id ?: repository.newId(),
            name = tilName.text(),
            breed = breed,
            type = type,
            price = price,
            abv = tilAbv.text().toIntOrNull() ?: 0,
            stock = stock,
            rating = current?.rating ?: 0f,
            sellerId = seller.id,
            sellerName = seller.farm,
            location = location ?: seller.location,
            icaRegistry = ica,
            // Si cambia el registro ICA vuelve a revisión (RF-26)
            status = if (current != null && current.icaRegistry == ica) current.status else ProductStatus.PENDIENTE,
            premium = current?.premium ?: false,
            description = tilDescription.text(),
            genotype = current?.genotype.orEmpty(),
            concentration = current?.concentration.orEmpty(),
            dose = current?.dose.orEmpty(),
            father = current?.father.orEmpty(),
            mother = current?.mother.orEmpty(),
            ageYears = tilAge.text().toIntOrNull() ?: 0,
            weightKg = tilWeight.text().toIntOrNull() ?: 0,
            darkCoat = current?.darkCoat ?: breed.equals(ANGUS, ignoreCase = true),
            hasCertificate = pdfName != null
        )
        repository.save(product)
        snack(if (current == null) R.string.form_published else R.string.form_updated)
        findNavController().navigateUp()
        }
    }

    private companion object {
        const val IMAGE_MIME = "image/*"
        const val PDF_MIME = "application/pdf"
        const val ANGUS = "Angus"
        const val DEFAULT_LAT = 8.7479
        const val DEFAULT_LNG = -75.8814
    }
}
