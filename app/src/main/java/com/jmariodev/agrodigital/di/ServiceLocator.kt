package com.jmariodev.agrodigital.di

import android.content.Context
import com.jmariodev.agrodigital.data.fake.FakeAuthRepository
import com.jmariodev.agrodigital.data.fake.FakeCartRepository
import com.jmariodev.agrodigital.data.fake.FakeOrderRepository
import com.jmariodev.agrodigital.data.fake.FakeProductRepository
import com.jmariodev.agrodigital.data.fake.FakeUserRepository
import com.jmariodev.agrodigital.data.model.User
import com.jmariodev.agrodigital.data.repository.AuthRepository
import com.jmariodev.agrodigital.data.repository.CartRepository
import com.jmariodev.agrodigital.data.repository.OrderRepository
import com.jmariodev.agrodigital.data.repository.ProductRepository
import com.jmariodev.agrodigital.data.repository.UserRepository
import com.jmariodev.agrodigital.data.session.SessionManager

/**
 * Punto único donde se decide qué implementación de cada repositorio usa la app.
 * Para conectar un backend real basta con cambiar las implementaciones aquí.
 */
object ServiceLocator {

    lateinit var session: SessionManager
        private set

    val userRepository: UserRepository by lazy { FakeUserRepository() }
    val authRepository: AuthRepository by lazy { FakeAuthRepository(userRepository) }
    val productRepository: ProductRepository by lazy { FakeProductRepository() }
    val orderRepository: OrderRepository by lazy { FakeOrderRepository() }
    val cartRepository: CartRepository by lazy { FakeCartRepository() }

    fun init(context: Context) {
        session = SessionManager(context.applicationContext)
    }

    /** Usuario con sesión activa (o null si no hay sesión). */
    fun currentUser(): User? = session.userId?.let { userRepository.getById(it) }
}
