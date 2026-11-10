package com.jmariodev.agrodigital.ui.common

import android.content.res.ColorStateList
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.data.model.OrderStatus
import com.jmariodev.agrodigital.data.model.ProductStatus
import com.jmariodev.agrodigital.data.model.ProductType
import com.jmariodev.agrodigital.data.model.UserRole
import com.jmariodev.agrodigital.data.model.UserStatus

/** Tonos de badge definidos en la guía de estilos. */
enum class BadgeTone(@ColorRes val background: Int, @ColorRes val text: Int) {
    SUCCESS(R.color.agro_primary_light, R.color.agro_primary_dark),
    INFO(R.color.agro_secondary_light, R.color.agro_secondary_dark),
    WARNING(R.color.agro_warning_light, R.color.agro_warning_dark),
    ERROR(R.color.agro_error_light, R.color.agro_error_dark),
    NEUTRAL(R.color.agro_neutral_light, R.color.agro_neutral_text),
    DARK(R.color.agro_neutral, R.color.agro_white)
}

fun TextView.setBadge(@StringRes label: Int, tone: BadgeTone) {
    setText(label)
    backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, tone.background))
    setTextColor(ContextCompat.getColor(context, tone.text))
}

fun TextView.setOrderStatus(status: OrderStatus) = when (status) {
    OrderStatus.PENDIENTE -> setBadge(R.string.status_pending, BadgeTone.WARNING)
    OrderStatus.PAGADO -> setBadge(R.string.status_paid, BadgeTone.WARNING)
    OrderStatus.ENVIADO -> setBadge(R.string.status_shipped, BadgeTone.INFO)
    OrderStatus.ENTREGADO -> setBadge(R.string.status_delivered, BadgeTone.SUCCESS)
    OrderStatus.CANCELADO -> setBadge(R.string.status_cancelled, BadgeTone.ERROR)
}

fun TextView.setProductStatus(status: ProductStatus) = when (status) {
    ProductStatus.PENDIENTE -> setBadge(R.string.status_pending, BadgeTone.WARNING)
    ProductStatus.APROBADO -> setBadge(R.string.status_approved, BadgeTone.SUCCESS)
    ProductStatus.RECHAZADO -> setBadge(R.string.status_rejected, BadgeTone.ERROR)
    ProductStatus.REPORTADO -> setBadge(R.string.status_reported, BadgeTone.ERROR)
}

fun TextView.setUserStatus(status: UserStatus) = when (status) {
    UserStatus.ACTIVO -> setBadge(R.string.status_active, BadgeTone.SUCCESS)
    UserStatus.PENDIENTE -> setBadge(R.string.status_pending, BadgeTone.WARNING)
    UserStatus.INACTIVO -> setBadge(R.string.status_inactive, BadgeTone.NEUTRAL)
}

fun TextView.setUserRole(role: UserRole) = when (role) {
    UserRole.COMPRADOR -> setBadge(R.string.role_buyer, BadgeTone.INFO)
    UserRole.VENDEDOR -> setBadge(R.string.role_seller, BadgeTone.SUCCESS)
    UserRole.ADMIN -> setBadge(R.string.role_admin, BadgeTone.DARK)
}

@StringRes
fun ProductType.label(): Int = when (this) {
    ProductType.PAJILLA -> R.string.type_semen
    ProductType.EMBRION -> R.string.type_embryo
    ProductType.EJEMPLAR -> R.string.type_live
}

@StringRes
fun UserRole.label(): Int = when (this) {
    UserRole.COMPRADOR -> R.string.role_buyer
    UserRole.VENDEDOR -> R.string.role_seller
    UserRole.ADMIN -> R.string.role_admin
}
