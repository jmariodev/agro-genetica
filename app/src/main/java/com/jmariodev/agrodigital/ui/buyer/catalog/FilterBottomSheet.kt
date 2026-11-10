package com.jmariodev.agrodigital.ui.buyer.catalog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.databinding.BottomSheetFiltersBinding
import com.jmariodev.agrodigital.ui.common.toCop

/** Hoja inferior con filtros de precio, raza y certificación. Devuelve el resultado al catálogo. */
class FilterBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFiltersBinding? = null
    private val binding get() = requireNotNull(_binding)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetFiltersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = requireArguments()
        val selected = args.getStringArrayList(KEY_BREEDS).orEmpty().toSet()

        binding.sliderPrice.setValues(args.getFloat(KEY_MIN), args.getFloat(KEY_MAX))
        updatePriceLabel()
        binding.sliderPrice.addOnChangeListener { _, _, _ -> updatePriceLabel() }

        args.getStringArrayList(KEY_ALL_BREEDS).orEmpty().forEach { breed ->
            val chip = layoutInflater.inflate(R.layout.view_filter_chip, binding.chipsBreed, false) as Chip
            chip.text = breed
            chip.isChecked = breed in selected
            binding.chipsBreed.addView(chip)
        }
        binding.cbCertified.isChecked = args.getBoolean(KEY_CERTIFIED)

        binding.btnClear.setOnClickListener {
            binding.sliderPrice.setValues(0f, CatalogViewModel.MAX_PRICE)
            binding.chipsBreed.clearCheck()
            binding.cbCertified.isChecked = false
        }
        binding.btnApply.setOnClickListener { applyAndDismiss() }
    }

    private fun updatePriceLabel() {
        val (min, max) = binding.sliderPrice.values
        binding.tvPriceRange.text = getString(R.string.price_range, min.toLong().toCop(), max.toLong().toCop())
    }

    private fun applyAndDismiss() {
        val (min, max) = binding.sliderPrice.values
        val breeds = ArrayList<String>()
        for (i in 0 until binding.chipsBreed.childCount) {
            val chip = binding.chipsBreed.getChildAt(i) as Chip
            if (chip.isChecked) breeds.add(chip.text.toString())
        }
        parentFragmentManager.setFragmentResult(
            RESULT_KEY,
            bundleOf(KEY_MIN to min, KEY_MAX to max, KEY_BREEDS to breeds, KEY_CERTIFIED to binding.cbCertified.isChecked)
        )
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "filters"
        const val RESULT_KEY = "catalog_filters"
        const val KEY_MIN = "min"
        const val KEY_MAX = "max"
        const val KEY_BREEDS = "breeds"
        const val KEY_ALL_BREEDS = "all_breeds"
        const val KEY_CERTIFIED = "certified"

        fun newInstance(min: Float, max: Float, allBreeds: List<String>, selected: Set<String>, certified: Boolean) =
            FilterBottomSheet().apply {
                arguments = bundleOf(
                    KEY_MIN to min,
                    KEY_MAX to max,
                    KEY_ALL_BREEDS to ArrayList(allBreeds),
                    KEY_BREEDS to ArrayList(selected),
                    KEY_CERTIFIED to certified
                )
            }
    }
}
