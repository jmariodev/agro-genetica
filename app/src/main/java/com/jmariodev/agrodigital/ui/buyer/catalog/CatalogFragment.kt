package com.jmariodev.agrodigital.ui.buyer.catalog

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.data.model.Product
import com.jmariodev.agrodigital.data.model.ProductType
import com.jmariodev.agrodigital.databinding.FragmentBuyerCatalogBinding
import com.jmariodev.agrodigital.di.ServiceLocator
import com.jmariodev.agrodigital.ui.common.BindingFragment
import com.jmariodev.agrodigital.ui.common.NavArgs
import com.jmariodev.agrodigital.ui.common.product.ProductGridAdapter
import com.jmariodev.agrodigital.ui.common.snack
import com.jmariodev.agrodigital.ui.common.visibleIf

/** Catálogo completo con búsqueda, filtros (hoja inferior) y orden. */
class CatalogFragment : BindingFragment<FragmentBuyerCatalogBinding>(FragmentBuyerCatalogBinding::inflate) {

    private val viewModel: CatalogViewModel by viewModels {
        viewModelFactory { initializer { CatalogViewModel(ServiceLocator.productRepository) } }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val type = arguments?.getString(NavArgs.TYPE)?.let { runCatching { ProductType.valueOf(it) }.getOrNull() }
            viewModel.setType(type)
        }
        childFragmentManager.setFragmentResultListener(FilterBottomSheet.RESULT_KEY, this) { _, result ->
            viewModel.setFilters(
                result.getFloat(FilterBottomSheet.KEY_MIN),
                result.getFloat(FilterBottomSheet.KEY_MAX),
                result.getStringArrayList(FilterBottomSheet.KEY_BREEDS).orEmpty().toSet(),
                result.getBoolean(FilterBottomSheet.KEY_CERTIFIED)
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = ProductGridAdapter(onClick = ::openProduct, onAddToCart = ::addToCart)
        binding.rvProducts.layoutManager = GridLayoutManager(requireContext(), resources.getInteger(R.integer.grid_span_count))
        binding.rvProducts.adapter = adapter

        binding.chipsType.check(
            when (viewModel.type) {
                ProductType.PAJILLA -> R.id.chipSemen
                ProductType.EMBRION -> R.id.chipEmbryo
                ProductType.EJEMPLAR -> R.id.chipLive
                null -> R.id.chipAll
            }
        )
        binding.chipsType.setOnCheckedStateChangeListener { _, checkedIds ->
            viewModel.setType(
                when (checkedIds.firstOrNull()) {
                    R.id.chipSemen -> ProductType.PAJILLA
                    R.id.chipEmbryo -> ProductType.EMBRION
                    R.id.chipLive -> ProductType.EJEMPLAR
                    else -> null
                }
            )
        }

        binding.etSearch.setText(viewModel.query)
        binding.etSearch.doAfterTextChanged { viewModel.setQuery(it?.toString().orEmpty()) }
        binding.btnFilters.setOnClickListener { openFilters() }
        binding.btnSort.setOnClickListener { showSortMenu(it) }

        with(binding.emptyState) {
            ivEmpty.setImageResource(R.drawable.ic_search)
            tvEmptyTitle.setText(R.string.empty_results_title)
            tvEmptyBody.setText(R.string.empty_results_body)
        }

        viewModel.results.observe(viewLifecycleOwner) { products ->
            adapter.submitList(products)
            binding.tvResultCount.text = resources.getQuantityString(R.plurals.results_count, products.size, products.size)
            binding.emptyState.root.visibleIf(products.isEmpty())
            binding.rvProducts.visibleIf(products.isNotEmpty())
        }

        if (savedInstanceState == null && arguments?.getBoolean(NavArgs.OPEN_FILTERS) == true) {
            arguments?.putBoolean(NavArgs.OPEN_FILTERS, false)
            openFilters()
        }
    }

    private fun openFilters() {
        FilterBottomSheet.newInstance(
            viewModel.minPrice, viewModel.maxPrice, viewModel.availableBreeds, viewModel.breeds, viewModel.onlyCertified
        ).show(childFragmentManager, FilterBottomSheet.TAG)
    }

    private fun showSortMenu(anchor: View) {
        PopupMenu(requireContext(), anchor).apply {
            menuInflater.inflate(R.menu.menu_sort, menu)
            setOnMenuItemClickListener { item ->
                viewModel.setSort(
                    when (item.itemId) {
                        R.id.sortPriceAsc -> SortOrder.PRICE_ASC
                        R.id.sortPriceDesc -> SortOrder.PRICE_DESC
                        R.id.sortRating -> SortOrder.RATING
                        R.id.sortAbv -> SortOrder.ABV
                        else -> SortOrder.RELEVANCE
                    }
                )
                binding.btnSort.text = item.title
                true
            }
            show()
        }
    }

    private fun openProduct(product: Product) {
        val action = if (product.isLiveAnimal) R.id.action_catalog_to_live else R.id.action_catalog_to_detail
        findNavController().navigate(action, bundleOf(NavArgs.PRODUCT_ID to product.id))
    }

    private fun addToCart(product: Product) {
        ServiceLocator.cartRepository.add(product)
        snack(getString(R.string.cart_added, product.name))
    }
}
