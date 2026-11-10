package com.jmariodev.agrodigital.ui.buyer.home

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.data.model.Product
import com.jmariodev.agrodigital.data.model.ProductType
import com.jmariodev.agrodigital.databinding.FragmentBuyerHomeBinding
import com.jmariodev.agrodigital.di.ServiceLocator
import com.jmariodev.agrodigital.ui.common.BindingFragment
import com.jmariodev.agrodigital.ui.common.NavArgs
import com.jmariodev.agrodigital.ui.common.RoleHostActivity
import com.jmariodev.agrodigital.ui.common.product.ProductGridAdapter
import com.jmariodev.agrodigital.ui.common.snack

/** Inicio del comprador: buscador, categorías, genética premium y ejemplares en exhibición. */
class HomeFragment : BindingFragment<FragmentBuyerHomeBinding>(FragmentBuyerHomeBinding::inflate) {

    private val productRepository = ServiceLocator.productRepository
    private val cartRepository = ServiceLocator.cartRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = ServiceLocator.currentUser()
        binding.tvGreeting.text = getString(R.string.home_greeting, user?.name?.substringBefore(' ').orEmpty())

        val premiumAdapter = ProductGridAdapter(horizontal = true, onClick = ::openProduct, onAddToCart = ::addToCart)
        val liveAdapter = ProductGridAdapter(horizontal = true, onClick = ::openProduct, onAddToCart = ::addToCart)
        binding.rvPremium.adapter = premiumAdapter
        binding.rvLive.adapter = liveAdapter

        val approved = productRepository.getApproved()
        premiumAdapter.submitList(approved.filter { !it.isLiveAnimal })
        liveAdapter.submitList(approved.filter { it.isLiveAnimal })

        binding.cardSearch.setOnClickListener { openCatalog() }
        binding.btnSeeAll.setOnClickListener { openCatalog() }
        binding.btnFilters.setOnClickListener { openCatalog(openFilters = true) }
        binding.chipSemen.setOnClickListener { openCatalog(ProductType.PAJILLA) }
        binding.chipEmbryo.setOnClickListener { openCatalog(ProductType.EMBRION) }
        binding.chipLive.setOnClickListener { openCatalog(ProductType.EJEMPLAR) }
    }

    private fun openCatalog(type: ProductType? = null, openFilters: Boolean = false) {
        val args = bundleOf(NavArgs.TYPE to type?.name, NavArgs.OPEN_FILTERS to openFilters)
        (requireActivity() as RoleHostActivity).openTopLevel(R.id.catalogFragment, args)
    }

    private fun openProduct(product: Product) {
        val args = bundleOf(NavArgs.PRODUCT_ID to product.id)
        val action = if (product.isLiveAnimal) R.id.action_home_to_live else R.id.action_home_to_detail
        findNavController().navigate(action, args)
    }

    private fun addToCart(product: Product) {
        cartRepository.add(product)
        snack(getString(R.string.cart_added, product.name))
    }
}
