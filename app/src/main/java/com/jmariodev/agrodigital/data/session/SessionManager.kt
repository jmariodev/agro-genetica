package com.jmariodev.agrodigital.data.session

import android.content.Context
import androidx.core.content.edit
import com.jmariodev.agrodigital.data.model.User
import com.jmariodev.agrodigital.data.model.UserRole

/**
 * Sesión local del usuario (rol y preferencia de huella).
 * La huella nunca sale del dispositivo: solo se guarda si el usuario la habilitó.
 */
class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val isLoggedIn: Boolean get() = prefs.getString(KEY_USER_ID, null) != null
    val userId: String? get() = prefs.getString(KEY_USER_ID, null)
    val lastUserId: String? get() = prefs.getString(KEY_LAST_USER_ID, null)

    val role: UserRole?
        get() = prefs.getString(KEY_ROLE, null)?.let { runCatching { UserRole.valueOf(it) }.getOrNull() }

    var biometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC, false)
        set(value) = prefs.edit { putBoolean(KEY_BIOMETRIC, value) }

    fun saveUser(user: User) {
        prefs.edit {
            putString(KEY_USER_ID, user.id)
            putString(KEY_LAST_USER_ID, user.id)
            putString(KEY_ROLE, user.role.name)
        }
    }

    /** Cierra la sesión. Conserva el último usuario para el ingreso con huella. */
    fun logout() {
        prefs.edit {
            remove(KEY_USER_ID)
            remove(KEY_ROLE)
        }
    }

    private companion object {
        const val PREFS_NAME = "agro_session"
        const val KEY_USER_ID = "user_id"
        const val KEY_LAST_USER_ID = "last_user_id"
        const val KEY_ROLE = "role"
        const val KEY_BIOMETRIC = "biometric_enabled"
    }
}
