package com.jmariodev.agrodigital.ui.buyer.cart

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.jmariodev.agrodigital.data.model.CartItem
import com.jmariodev.agrodigital.data.repository.CartRepository

/** Compartido entre carrito y checkout (activityViewModels). */
class CartViewModel(private val repository: CartRepository) : ViewModel() {
    val items: LiveData<List<CartItem>> = repository.items

    fun increase(item: CartItem) = repository.updateQuantity(item.product.id, item.quantity + 1)
    fun decrease(item: CartItem) = repository.updateQuantity(item.product.id, item.quantity - 1)
    fun remove(item: CartItem) = repository.remove(item.product.id)
    fun restore(item: CartItem) = repository.add(item.product, item.quantity)
    fun clear() = repository.clear()

    fun subtotal(): Long = repository.subtotal()
    fun shipping(): Long = repository.shipping()
    fun total(): Long = subtotal() + shipping()
    fun count(): Int = repository.count()
}
