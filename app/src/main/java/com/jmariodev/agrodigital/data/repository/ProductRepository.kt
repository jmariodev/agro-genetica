package com.jmariodev.agrodigital.data.repository

import com.jmariodev.agrodigital.data.model.Product
import com.jmariodev.agrodigital.data.model.ProductStatus

interface ProductRepository {
    fun getAll(): List<Product>
    fun getApproved(): List<Product>
    fun getById(id: String): Product?
    fun getBySeller(sellerId: String): List<Product>
    fun getByStatus(status: ProductStatus): List<Product>
    fun save(product: Product): Product
    fun delete(id: String)
    fun setStatus(id: String, status: ProductStatus, reason: String? = null)
    fun newId(): String
}
