package com.jmariodev.agrodigital.ui.common

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.jmariodev.agrodigital.R
import java.text.NumberFormat
import java.util.Locale

private val copFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-CO"))

/** 450000 -> "$450.000" (pesos colombianos). */
fun Long.toCop(): String = "$" + copFormat.format(this)

fun View.visibleIf(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

fun Fragment.snack(@StringRes message: Int) {
    view?.let { Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show() }
}

fun Fragment.snack(message: String) {
    view?.let { Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show() }
}

/** Toast corto: retroalimentación inmediata de errores de pago (RNF < 1 s). */
fun Context.toast(@StringRes message: Int) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun TextInputLayout.text(): String = editText?.text?.toString()?.trim().orEmpty()

/** Valida que el campo no esté vacío y muestra el error en el propio TextInputLayout. */
fun TextInputLayout.requireText(@StringRes error: Int): Boolean {
    val ok = text().isNotEmpty()
    this.error = if (ok) null else context.getString(error)
    return ok
}

/** Pinta la silueta del animal con el color de su pelaje. */
fun ImageView.tintCoat(darkCoat: Boolean) {
    val color = if (darkCoat) R.color.agro_bull_dark else R.color.agro_bull_brown
    imageTintList = ContextCompat.getColorStateList(context, color)
}
