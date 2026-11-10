package com.jmariodev.agrodigital.ui.common.profile

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.databinding.FragmentEditProfileBinding
import com.jmariodev.agrodigital.di.ServiceLocator
import com.jmariodev.agrodigital.ui.common.BindingFragment
import com.jmariodev.agrodigital.ui.common.requireText
import com.jmariodev.agrodigital.ui.common.snack
import com.jmariodev.agrodigital.ui.common.text
import java.util.Locale

/** Edición de perfil con foto (cámara nativa o galería) y ubicación GPS de la finca (RF-18, RF-19). */
class EditProfileFragment : BindingFragment<FragmentEditProfileBinding>(FragmentEditProfileBinding::inflate) {

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            binding.ivAvatar.imageTintList = null
            binding.ivAvatar.setPadding(0, 0, 0, 0)
            binding.ivAvatar.setImageBitmap(bitmap)
        }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            binding.ivAvatar.imageTintList = null
            binding.ivAvatar.setPadding(0, 0, 0, 0)
            binding.ivAvatar.setImageURI(uri)
        }
    }

    private val locationPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result.values.any { it }) fillLocation() else snack(R.string.location_denied)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = ServiceLocator.currentUser() ?: return
        binding.tilName.editText?.setText(user.name)
        binding.tilEmail.editText?.setText(user.email)
        binding.tilPhone.editText?.setText(user.phone)
        binding.tilFarm.editText?.setText(user.farm)
        binding.tilLocation.editText?.setText(user.location)

        binding.btnChangePhoto.setOnClickListener { choosePhotoSource() }
        binding.ivAvatar.setOnClickListener { choosePhotoSource() }
        binding.tilLocation.setEndIconOnClickListener { requestLocation() }
        binding.btnSave.setOnClickListener { save() }
    }

    private fun choosePhotoSource() {
        val options = arrayOf(getString(R.string.photo_camera), getString(R.string.photo_gallery))
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.photo_choose)
            .setItems(options) { _, which ->
                if (which == 0) takePicture.launch(null) else pickImage.launch(IMAGE_MIME)
            }
            .show()
    }

    private fun requestLocation() {
        val granted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) {
            fillLocation()
        } else {
            locationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    /** Una sola lectura de ubicación (no actualizaciones continuas) para cuidar la batería. */
    @SuppressLint("MissingPermission")
    private fun fillLocation() {
        val manager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location: Location? = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { manager.isProviderEnabled(it) }
            .firstNotNullOfOrNull { manager.getLastKnownLocation(it) }
        val (lat, lng) = if (location != null) location.latitude to location.longitude else DEFAULT_LAT to DEFAULT_LNG
        val coords = String.format(Locale.US, "%.4f, %.4f", lat, lng)
        binding.tilLocation.editText?.setText(getString(R.string.location_coords, coords))
        snack(if (location != null) R.string.location_updated else R.string.location_default)
    }

    private fun save() {
        val user = ServiceLocator.currentUser() ?: return
        val nameOk = binding.tilName.requireText(R.string.error_required)
        val phoneOk = binding.tilPhone.requireText(R.string.error_required)
        if (!nameOk || !phoneOk) return
        ServiceLocator.userRepository.save(
            user.copy(
                name = binding.tilName.text(),
                phone = binding.tilPhone.text(),
                farm = binding.tilFarm.text(),
                location = binding.tilLocation.text()
            )
        )
        snack(R.string.profile_saved)
        findNavController().navigateUp()
    }

    private companion object {
        const val IMAGE_MIME = "image/*"
        const val DEFAULT_LAT = 8.7479
        const val DEFAULT_LNG = -75.8814
    }
}
