package com.jmariodev.agrodigital.ui.common

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.jmariodev.agrodigital.databinding.ViewStatCardBinding

/** Rellena una tarjeta de métrica del dashboard. */
fun ViewStatCardBinding.set(
    @DrawableRes icon: Int,
    value: String,
    label: String,
    trend: String? = null,
    @ColorRes tint: Int = com.jmariodev.agrodigital.R.color.agro_primary,
    @ColorRes background: Int = com.jmariodev.agrodigital.R.color.agro_primary_light
) {
    val context = root.context
    ivIcon.setImageResource(icon)
    ivIcon.imageTintList = ContextCompat.getColorStateList(context, tint)
    ivIcon.backgroundTintList = ContextCompat.getColorStateList(context, background)
    tvValue.text = value
    tvLabel.text = label
    tvTrend.text = trend.orEmpty()
    tvTrend.visibleIf(trend != null)
}

/** 4_500_000 -> "$4.5M", 45_000 -> "$45K". */
fun Long.toCompactCop(): String = when {
    this >= 1_000_000 -> "$" + String.format(java.util.Locale.US, "%.1fM", this / 1_000_000.0).replace(".0M", "M")
    this >= 1_000 -> "$" + (this / 1_000) + "K"
    else -> toCop()
}
