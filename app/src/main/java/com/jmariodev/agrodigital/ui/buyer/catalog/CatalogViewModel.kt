package com.jmariodev.agrodigital.ui.buyer.catalog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jmariodev.agrodigital.data.model.Product
import com.jmariodev.agrodigital.data.model.ProductType
import com.jmariodev.agrodigital.data.repository.ProductRepository

enum class SortOrder { RELEVANCE, PRICE_ASC, PRICE_DESC, RATING, ABV }

/** Estado del catálogo: búsqueda, tipo, filtros de precio/raza y orden. */
class CatalogViewModel(private val repository: ProductRepository) : ViewModel() {

    var query: String = ""
        private set
    var type: ProductType? = null
        private set
    var minPrice: Float = 0f
        private set
    var maxPrice: Float = MAX_PRICE
        private set
    var breeds: Set<String> = emptySet()
        private set
    var onlyCertified: Boolean = false
        private set
    private var sort = SortOrder.RELEVANCE

    private val _results = MutableLiveData<List<Product>>()
    val results: LiveData<List<Product>> = _results

    val availableBreeds: List<String>
        get() = repository.getApproved().map { it.breed }.distinct().sorted()

    init {
        apply()
    }

    fun setQuery(value: String) { query = value; apply() }
    fun setType(value: ProductType?) { type = value; apply() }
    fun setSort(value: SortOrder) { sort = value; apply() }

    fun setFilters(min: Float, max: Float, selectedBreeds: Set<String>, certified: Boolean) {
        minPrice = min
        maxPrice = max
        breeds = selectedBreeds
        onlyCertified = certified
        apply()
    }

    private fun apply() {
        val text = query.trim().lowercase()
        val filtered = repository.getApproved().filter { p ->
            (type == null || p.type == type) &&
                (text.isEmpty() || p.name.lowercase().contains(text) || p.breed.lowercase().contains(text)) &&
                (p.isLiveAnimal || p.price.toFloat() in minPrice..maxPrice) &&
                (breeds.isEmpty() || p.breed in breeds) &&
                (!onlyCertified || p.hasCertificate)
        }
        _results.value = when (sort) {
            SortOrder.RELEVANCE -> filtered
            SortOrder.PRICE_ASC -> filtered.sortedBy { it.price }
            SortOrder.PRICE_DESC -> filtered.sortedByDescending { it.price }
            SortOrder.RATING -> filtered.sortedByDescending { it.rating }
            SortOrder.ABV -> filtered.sortedByDescending { it.abv }
        }
    }

    companion object {
        const val MAX_PRICE = 1_000_000f
    }
}
