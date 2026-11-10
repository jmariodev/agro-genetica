package com.jmariodev.agrodigital.data.repository

import com.jmariodev.agrodigital.data.model.User
import com.jmariodev.agrodigital.data.model.UserStatus

interface UserRepository {
    fun getAll(): List<User>
    fun getById(id: String): User?
    fun getByEmail(email: String): User?
    fun save(user: User): User
    fun delete(id: String)
    fun setStatus(id: String, status: UserStatus)
    fun newId(): String
}
