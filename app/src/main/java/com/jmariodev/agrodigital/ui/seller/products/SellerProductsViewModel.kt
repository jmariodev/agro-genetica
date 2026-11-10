package com.jmariodev.agrodigital.ui.seller.products

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jmariodev.agrodigital.data.model.Product
import com.jmariodev.agrodigital.data.model.ProductStatus
import com.jmariodev.agrodigital.data.repository.ProductRepository

/** Lista de productos del vendedor con búsqueda, filtro por estado y eliminación con deshacer. */
class SellerProductsViewModel(
    private val repository: ProductRepository,
    private val sellerId: String
) : ViewModel() {

    private var query = ""
    private var status: ProductStatus? = null

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    fun refresh() {
        val text = query.trim().lowercase()
        _products.value = repository.getBySeller(sellerId).filter { p ->
            (status == null || p.status == status) &&
                (text.isEmpty() || p.name.lowercase().contains(text) || p.breed.lowercase().contains(text))
        }
    }

    fun setQuery(value: String) { query = value; refresh() }
    fun setStatus(value: ProductStatus?) { status = value; refresh() }

    fun delete(product: Product) {
        repository.delete(product.id)
        refresh()
    }

    fun restore(product: Product) {
        repository.save(product)
        refresh()
    }
}
