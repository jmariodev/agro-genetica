package com.jmariodev.agrodigital.ui.auth

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.jmariodev.agrodigital.data.model.User
import com.jmariodev.agrodigital.databinding.ActivityAuthBinding
import com.jmariodev.agrodigital.di.ServiceLocator
import com.jmariodev.agrodigital.ui.common.RoleRouter
import com.jmariodev.agrodigital.ui.common.applyStatusBarPadding

/** Aloja el grafo nav_auth: login, registro, recuperación de contraseña y huella. */
class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        super.onCreate(savedInstanceState)
        val binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applyStatusBarPadding()
    }
}

/** Guarda la sesión y abre la Activity correspondiente al rol. */
fun Fragment.openRoleHome(user: User) {
    ServiceLocator.session.saveUser(user)
    startActivity(RoleRouter.intentFor(requireContext(), user.role))
    requireActivity().finish()
}
