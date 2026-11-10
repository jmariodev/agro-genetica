package com.jmariodev.agrodigital.ui.splash

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.databinding.ActivitySplashBinding
import com.jmariodev.agrodigital.di.ServiceLocator
import com.jmariodev.agrodigital.ui.common.RoleRouter

/** Pantalla de bienvenida: muestra la marca y redirige según la sesión guardada. */
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val handler = Handler(Looper.getMainLooper())
    private val goNext = Runnable { navigateNext() }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.logoContainer.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_scale_in))
        binding.tvBrand.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in))
        binding.tvBrandSecond.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in))

        handler.postDelayed(goNext, resources.getInteger(R.integer.splash_delay_ms).toLong())
    }

    private fun navigateNext() {
        val session = ServiceLocator.session
        val role = if (session.isLoggedIn && ServiceLocator.currentUser() != null) session.role else null
        startActivity(RoleRouter.intentFor(this, role))
        finish()
    }

    override fun onDestroy() {
        handler.removeCallbacks(goNext)
        super.onDestroy()
    }
}
