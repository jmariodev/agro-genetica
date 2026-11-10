package com.jmariodev.agrodigital.ui.common

import android.content.Context
import android.content.Intent
import com.jmariodev.agrodigital.data.model.UserRole
import com.jmariodev.agrodigital.ui.admin.AdminActivity
import com.jmariodev.agrodigital.ui.auth.AuthActivity
import com.jmariodev.agrodigital.ui.buyer.BuyerActivity
import com.jmariodev.agrodigital.ui.seller.SellerActivity

/** Decide a qué Activity va cada rol después de autenticarse. */
object RoleRouter {

    fun intentFor(context: Context, role: UserRole?): Intent {
        val target = when (role) {
            UserRole.COMPRADOR -> BuyerActivity::class.java
            UserRole.VENDEDOR -> SellerActivity::class.java
            UserRole.ADMIN -> AdminActivity::class.java
            null -> AuthActivity::class.java
        }
        return Intent(context, target).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }
}
