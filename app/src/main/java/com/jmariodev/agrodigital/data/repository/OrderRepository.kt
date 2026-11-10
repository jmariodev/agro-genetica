package com.jmariodev.agrodigital.data.repository

import com.jmariodev.agrodigital.data.model.CartItem
import com.jmariodev.agrodigital.data.model.Order
import com.jmariodev.agrodigital.data.model.OrderStatus
import com.jmariodev.agrodigital.data.model.RankingItem
import com.jmariodev.agrodigital.data.model.ReportPeriod
import com.jmariodev.agrodigital.data.model.SalesPoint

interface OrderRepository {
    fun getAll(): List<Order>
    fun getByBuyer(buyerId: String): List<Order>
    fun getBySeller(sellerId: String): List<Order>
    fun getById(id: String): Order?
    fun create(buyerId: String, buyerName: String, items: List<CartItem>, shipping: Long, address: String, paymentMethod: String): Order
    fun updateStatus(id: String, status: OrderStatus)
    fun salesSeries(period: ReportPeriod): List<SalesPoint>
    fun topProducts(period: ReportPeriod): List<RankingItem>
    fun topSellers(period: ReportPeriod): List<RankingItem>
}
