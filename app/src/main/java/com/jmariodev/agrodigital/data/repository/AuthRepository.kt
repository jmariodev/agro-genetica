package com.jmariodev.agrodigital.data.repository

import com.jmariodev.agrodigital.data.model.User
import com.jmariodev.agrodigital.data.model.UserRole

/** Autenticación. Hoy se resuelve con datos simulados; mañana con un proveedor real. */
interface AuthRepository {
    fun login(email: String, password: String): User?
    fun register(name: String, email: String, phone: String, farm: String, role: UserRole, password: String): User
    fun recoverPassword(email: String): Boolean
    fun demoUser(role: UserRole): User
}
