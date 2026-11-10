package com.jmariodev.agrodigital.ui.common

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.MenuRes
import androidx.annotation.NavigationRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.databinding.ActivityRoleHostBinding
import com.jmariodev.agrodigital.databinding.NavHeaderDrawerBinding
import com.jmariodev.agrodigital.di.ServiceLocator
import com.jmariodev.agrodigital.ui.auth.AuthActivity

/**
 * Base de las Activities por rol. Cada rol solo declara su grafo de navegación
 * y sus menús (Bottom Navigation + Drawer); el comportamiento es común.
 */
abstract class RoleHostActivity : AppCompatActivity() {

    @get:NavigationRes protected abstract val graphRes: Int
    @get:MenuRes protected abstract val bottomMenuRes: Int
    @get:MenuRes protected abstract val drawerMenuRes: Int

    protected lateinit var binding: ActivityRoleHostBinding
    protected lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private var navigationBarInset = 0

    /** false cuando no hay sesión y la Activity se cierra para volver al login. */
    protected val isReady: Boolean get() = ::binding.isInitialized

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        super.onCreate(savedInstanceState)
        val user = ServiceLocator.currentUser()
        if (user == null) {
            goToLogin()
            return
        }
        binding = ActivityRoleHostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val navHost = supportFragmentManager.findFragmentById(binding.navHostContainer.id) as NavHostFragment
        navController = navHost.navController
        if (savedInstanceState == null || navController.currentDestination == null) {
            navController.setGraph(graphRes)
        }

        binding.bottomNav.inflateMenu(bottomMenuRes)
        binding.navigationView.inflateMenu(drawerMenuRes)

        val topLevel = (0 until binding.bottomNav.menu.size()).map { binding.bottomNav.menu.getItem(it).itemId }.toSet()
        appBarConfiguration = AppBarConfiguration(topLevel, binding.drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNav.setupWithNavController(navController)
        setupDrawer()
        bindDrawerHeader()
        setupInsets()

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isTopLevel = destination.id in topLevel
            binding.bottomNav.visibleIf(isTopLevel)
            applyContentInset()
            binding.navigationView.setCheckedItem(destination.id)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else if (!navController.popBackStack()) {
                    finish()
                }
            }
        })
    }

    private fun setupDrawer() {
        binding.navigationView.setNavigationItemSelectedListener { item ->
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            if (item.itemId == R.id.action_logout) {
                confirmLogout()
            } else {
                NavigationUI.onNavDestinationSelected(item, navController)
            }
            true
        }
    }

    private fun bindDrawerHeader() {
        val user = ServiceLocator.currentUser() ?: return
        val header = NavHeaderDrawerBinding.bind(binding.navigationView.getHeaderView(0))
        header.tvName.text = user.name
        header.tvFarm.text = user.farm
        header.tvRole.setUserRole(user.role)
    }

    /** Barra de estado sobre la Toolbar; barra de navegación bajo el contenido cuando no hay Bottom Nav. */
    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.contentRoot) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.appBar.updatePadding(top = bars.top)
            navigationBarInset = bars.bottom
            applyContentInset()
            insets
        }
    }

    private fun applyContentInset() {
        val bottom = if (binding.bottomNav.visibility == View.VISIBLE) 0 else navigationBarInset
        binding.navHostContainer.updatePadding(bottom = bottom)
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()

    /**
     * Abre una pestaña principal (destino de la Bottom Navigation) con argumentos opcionales,
     * sin apilar destinos duplicados.
     */
    fun openTopLevel(destinationId: Int, args: Bundle? = null) {
        val options = NavOptions.Builder()
            .setPopUpTo(navController.graph.startDestinationId, false, false)
            .setLaunchSingleTop(true)
            .build()
        navController.navigate(destinationId, args, options)
    }

    fun confirmLogout() {
        MaterialAlertDialogBuilder(this)
            .setIcon(R.drawable.ic_logout)
            .setTitle(R.string.logout_title)
            .setMessage(R.string.logout_message)
            .setPositiveButton(R.string.logout_confirm) { _, _ ->
                ServiceLocator.session.logout()
                ServiceLocator.cartRepository.clear()
                goToLogin()
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun goToLogin() {
        startActivity(
            Intent(this, AuthActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        finish()
    }
}
