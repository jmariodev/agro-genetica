package com.jmariodev.agrodigital.data.repository

import androidx.lifecycle.LiveData
import com.jmariodev.agrodigital.data.model.CartItem
import com.jmariodev.agrodigital.data.model.Product

interface CartRepository {
    val items: LiveData<List<CartItem>>
    fun add(product: Product, quantity: Int = 1)
    fun updateQuantity(productId: String, quantity: Int)
    fun remove(productId: String)
    fun clear()
    fun subtotal(): Long
    fun shipping(): Long
    fun count(): Int
}
