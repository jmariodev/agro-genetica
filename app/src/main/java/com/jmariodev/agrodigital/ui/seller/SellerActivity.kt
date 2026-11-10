package com.jmariodev.agrodigital.ui.seller

import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.ui.common.RoleHostActivity

/** Rol Vendedor: Panel, Productos, Pedidos y Perfil. */
class SellerActivity : RoleHostActivity() {
    override val graphRes = R.navigation.nav_seller
    override val bottomMenuRes = R.menu.menu_seller_bottom
    override val drawerMenuRes = R.menu.menu_seller_drawer
}
