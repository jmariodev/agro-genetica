package com.jmariodev.agrodigital.ui.buyer

import android.os.Bundle
import androidx.core.content.ContextCompat
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.di.ServiceLocator
import com.jmariodev.agrodigital.ui.common.RoleHostActivity

/** Rol Comprador: Inicio, Buscar, Carrito y Perfil. */
class BuyerActivity : RoleHostActivity() {
    override val graphRes = R.navigation.nav_buyer
    override val bottomMenuRes = R.menu.menu_buyer_bottom
    override val drawerMenuRes = R.menu.menu_buyer_drawer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isReady) return
        // Insignia con la cantidad de productos en el carrito
        ServiceLocator.cartRepository.items.observe(this) { items ->
            val count = items.sumOf { it.quantity }
            if (count > 0) {
                binding.bottomNav.getOrCreateBadge(R.id.cartFragment).apply {
                    number = count
                    backgroundColor = ContextCompat.getColor(this@BuyerActivity, R.color.agro_error)
                }
            } else {
                binding.bottomNav.removeBadge(R.id.cartFragment)
            }
        }
    }
}
