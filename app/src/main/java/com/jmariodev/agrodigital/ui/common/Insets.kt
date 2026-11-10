package com.jmariodev.agrodigital.ui.common

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/** Aplica el alto de la barra de estado como padding superior (diseño de borde a borde). */
fun View.applyStatusBarPadding() {
    val initialTop = paddingTop
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updatePadding(top = initialTop + bars.top)
        insets
    }
}
