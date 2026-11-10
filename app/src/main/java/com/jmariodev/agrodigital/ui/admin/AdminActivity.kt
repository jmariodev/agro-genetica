package com.jmariodev.agrodigital.ui.admin

import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.ui.common.RoleHostActivity

/** Rol Administrador: Panel, Usuarios, Productos y Reportes. */
class AdminActivity : RoleHostActivity() {
    override val graphRes = R.navigation.nav_admin
    override val bottomMenuRes = R.menu.menu_admin_bottom
    override val drawerMenuRes = R.menu.menu_admin_drawer
}
