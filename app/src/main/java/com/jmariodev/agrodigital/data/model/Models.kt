package com.jmariodev.agrodigital.data.model

/** Tipos de producto que se comercializan en la plataforma. */
enum class ProductType { PAJILLA, EMBRION, EJEMPLAR }

/** Estados de moderación de una publicación (RF-21). */
enum class ProductStatus { PENDIENTE, APROBADO, RECHAZADO, REPORTADO }

/** Estados de un pedido (RF-36). */
enum class OrderStatus { PENDIENTE, PAGADO, ENVIADO, ENTREGADO, CANCELADO }

/** Roles del sistema. */
enum class UserRole { COMPRADOR, VENDEDOR, ADMIN }

/** Estados de una cuenta de usuario (RF-10). */
enum class UserStatus { ACTIVO, PENDIENTE, INACTIVO }

/** Periodos del reporte de ventas del administrador. */
enum class ReportPeriod { SEMANA, MES, ANIO }

data class Product(
    val id: String,
    val name: String,
    val breed: String,
    val type: ProductType,
    val price: Long,
    val abv: Int,
    val stock: Int,
    val rating: Float,
    val sellerId: String,
    val sellerName: String,
    val location: String,
    val icaRegistry: String,
    val status: ProductStatus = ProductStatus.APROBADO,
    val premium: Boolean = false,
    val description: String = "",
    val genotype: String = "",
    val concentration: String = "",
    val dose: String = "",
    val father: String = "",
    val mother: String = "",
    val ageYears: Int = 0,
    val weightKg: Int = 0,
    val reportReason: String? = null,
    val createdLabel: String = "Hoy",
    val darkCoat: Boolean = false,
    val hasCertificate: Boolean = true
) {
    val isLiveAnimal: Boolean get() = type == ProductType.EJEMPLAR
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: UserRole,
    val status: UserStatus,
    val farm: String,
    val location: String,
    val verified: Boolean = false,
    val createdLabel: String = "Hoy"
)

data class CartItem(
    val product: Product,
    val quantity: Int
) {
    val total: Long get() = product.price * quantity
}

data class OrderItem(
    val productId: String,
    val productName: String,
    val breed: String,
    val type: ProductType,
    val quantity: Int,
    val unitPrice: Long,
    val darkCoat: Boolean = false
) {
    val total: Long get() = unitPrice * quantity
}

data class Order(
    val id: String,
    val code: String,
    val date: String,
    val status: OrderStatus,
    val items: List<OrderItem>,
    val shipping: Long,
    val buyerId: String,
    val buyerName: String,
    val sellerId: String,
    val sellerName: String,
    val address: String,
    val paymentMethod: String
) {
    val subtotal: Long get() = items.sumOf { it.total }
    val total: Long get() = subtotal + shipping
    val itemCount: Int get() = items.sumOf { it.quantity }
}

/** Punto de una serie del reporte de ventas. */
data class SalesPoint(val label: String, val amount: Long)

/** Fila de ranking (productos o vendedores más vendidos). */
data class RankingItem(val name: String, val detail: String, val amount: Long, val percent: Int)
