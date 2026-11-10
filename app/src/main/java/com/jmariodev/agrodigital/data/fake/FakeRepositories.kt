package com.jmariodev.agrodigital.data.fake

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jmariodev.agrodigital.data.model.CartItem
import com.jmariodev.agrodigital.data.model.Order
import com.jmariodev.agrodigital.data.model.OrderItem
import com.jmariodev.agrodigital.data.model.OrderStatus
import com.jmariodev.agrodigital.data.model.Product
import com.jmariodev.agrodigital.data.model.ProductStatus
import com.jmariodev.agrodigital.data.model.RankingItem
import com.jmariodev.agrodigital.data.model.ReportPeriod
import com.jmariodev.agrodigital.data.model.SalesPoint
import com.jmariodev.agrodigital.data.model.User
import com.jmariodev.agrodigital.data.model.UserRole
import com.jmariodev.agrodigital.data.model.UserStatus
import com.jmariodev.agrodigital.data.repository.AuthRepository
import com.jmariodev.agrodigital.data.repository.CartRepository
import com.jmariodev.agrodigital.data.repository.OrderRepository
import com.jmariodev.agrodigital.data.repository.ProductRepository
import com.jmariodev.agrodigital.data.repository.UserRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Implementaciones en memoria de los repositorios (solo frontend, sin backend). */

class FakeUserRepository : UserRepository {
    private val users = FakeData.users.toMutableList()
    private var sequence = 100

    override fun getAll(): List<User> = users.toList()
    override fun getById(id: String): User? = users.firstOrNull { it.id == id }
    override fun getByEmail(email: String): User? = users.firstOrNull { it.email.equals(email.trim(), ignoreCase = true) }

    override fun save(user: User): User {
        val index = users.indexOfFirst { it.id == user.id }
        if (index >= 0) users[index] = user else users.add(0, user)
        return user
    }

    override fun delete(id: String) {
        users.removeAll { it.id == id }
    }

    override fun setStatus(id: String, status: UserStatus) {
        getById(id)?.let { save(it.copy(status = status)) }
    }

    override fun newId(): String = "u_${sequence++}"
}

class FakeAuthRepository(private val userRepository: UserRepository) : AuthRepository {

    override fun login(email: String, password: String): User? {
        val user = userRepository.getByEmail(email) ?: return null
        return if (password == FakeData.DEMO_PASSWORD && user.status != UserStatus.INACTIVO) user else null
    }

    override fun register(name: String, email: String, phone: String, farm: String, role: UserRole, password: String): User {
        val status = if (role == UserRole.VENDEDOR) UserStatus.PENDIENTE else UserStatus.ACTIVO
        val user = User(userRepository.newId(), name, email, phone, role, status, farm, farm)
        return userRepository.save(user)
    }

    override fun recoverPassword(email: String): Boolean = true

    override fun demoUser(role: UserRole): User {
        val id = when (role) {
            UserRole.COMPRADOR -> FakeData.BUYER_ID
            UserRole.VENDEDOR -> FakeData.SELLER_ID
            UserRole.ADMIN -> FakeData.ADMIN_ID
        }
        return requireNotNull(userRepository.getById(id))
    }
}

class FakeProductRepository : ProductRepository {
    private val products = FakeData.products.toMutableList()
    private var sequence = 100

    override fun getAll(): List<Product> = products.toList()
    override fun getApproved(): List<Product> = products.filter { it.status == ProductStatus.APROBADO }
    override fun getById(id: String): Product? = products.firstOrNull { it.id == id }
    override fun getBySeller(sellerId: String): List<Product> = products.filter { it.sellerId == sellerId }
    override fun getByStatus(status: ProductStatus): List<Product> = products.filter { it.status == status }

    override fun save(product: Product): Product {
        val index = products.indexOfFirst { it.id == product.id }
        if (index >= 0) products[index] = product else products.add(0, product)
        return product
    }

    override fun delete(id: String) {
        products.removeAll { it.id == id }
    }

    override fun setStatus(id: String, status: ProductStatus, reason: String?) {
        getById(id)?.let { save(it.copy(status = status, reportReason = reason ?: it.reportReason)) }
    }

    override fun newId(): String = "p_${sequence++}"
}

class FakeOrderRepository : OrderRepository {
    private val orders = FakeData.orders.toMutableList()
    private var sequence = 7

    override fun getAll(): List<Order> = orders.toList()
    override fun getByBuyer(buyerId: String): List<Order> = orders.filter { it.buyerId == buyerId }
    override fun getBySeller(sellerId: String): List<Order> = orders.filter { it.sellerId == sellerId }
    override fun getById(id: String): Order? = orders.firstOrNull { it.id == id }

    override fun create(
        buyerId: String,
        buyerName: String,
        items: List<CartItem>,
        shipping: Long,
        address: String,
        paymentMethod: String
    ): Order {
        val first = items.first().product
        val code = "ORD-2026-%03d".format(sequence++)
        val date = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("es-CO")).format(Date())
        val order = Order(
            id = "o_$code",
            code = code,
            date = date,
            status = OrderStatus.PAGADO,
            items = items.map { OrderItem(it.product.id, it.product.name, it.product.breed, it.product.type, it.quantity, it.product.price, it.product.darkCoat) },
            shipping = shipping,
            buyerId = buyerId,
            buyerName = buyerName,
            sellerId = first.sellerId,
            sellerName = first.sellerName,
            address = address,
            paymentMethod = paymentMethod
        )
        orders.add(0, order)
        return order
    }

    override fun updateStatus(id: String, status: OrderStatus) {
        val index = orders.indexOfFirst { it.id == id }
        if (index >= 0) orders[index] = orders[index].copy(status = status)
    }

    override fun salesSeries(period: ReportPeriod): List<SalesPoint> = FakeData.salesSeries(period)
    override fun topProducts(period: ReportPeriod): List<RankingItem> = FakeData.topProducts(period)
    override fun topSellers(period: ReportPeriod): List<RankingItem> = FakeData.topSellers(period)
}

class FakeCartRepository : CartRepository {
    private val cart = mutableListOf<CartItem>()
    private val _items = MutableLiveData<List<CartItem>>(emptyList())
    override val items: LiveData<List<CartItem>> = _items

    private fun publish() {
        _items.value = cart.toList()
    }

    override fun add(product: Product, quantity: Int) {
        val index = cart.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val current = cart[index]
            cart[index] = current.copy(quantity = (current.quantity + quantity).coerceAtMost(product.stock))
        } else {
            cart.add(CartItem(product, quantity.coerceAtMost(product.stock)))
        }
        publish()
    }

    override fun updateQuantity(productId: String, quantity: Int) {
        val index = cart.indexOfFirst { it.product.id == productId }
        if (index < 0) return
        val item = cart[index]
        cart[index] = item.copy(quantity = quantity.coerceIn(1, item.product.stock))
        publish()
    }

    override fun remove(productId: String) {
        cart.removeAll { it.product.id == productId }
        publish()
    }

    override fun clear() {
        cart.clear()
        publish()
    }

    override fun subtotal(): Long = cart.sumOf { it.total }
    override fun shipping(): Long = if (cart.isEmpty()) 0 else FakeData.SHIPPING_FEE
    override fun count(): Int = cart.sumOf { it.quantity }
}
