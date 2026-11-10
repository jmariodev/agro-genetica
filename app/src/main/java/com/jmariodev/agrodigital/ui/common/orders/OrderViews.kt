package com.jmariodev.agrodigital.ui.common.orders

import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.widget.TextViewCompat
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.data.model.OrderItem
import com.jmariodev.agrodigital.databinding.ItemCheckoutProductBinding
import com.jmariodev.agrodigital.databinding.ViewPriceRowBinding
import com.jmariodev.agrodigital.ui.common.label
import com.jmariodev.agrodigital.ui.common.tintCoat
import com.jmariodev.agrodigital.ui.common.toCop

/** Pinta las líneas de producto de un resumen de pedido dentro de [container]. */
fun LinearLayout.bindOrderItems(items: List<OrderItem>) {
    removeAllViews()
    val inflater = LayoutInflater.from(context)
    items.forEach { item ->
        val row = ItemCheckoutProductBinding.inflate(inflater, this, true)
        row.tvName.text = context.getString(R.string.item_quantity_format, item.productName, item.quantity)
        row.tvType.text = context.getString(R.string.item_type_breed, context.getString(item.type.label()), item.breed)
        row.tvTotal.text = item.total.toCop()
        row.ivProduct.tintCoat(item.darkCoat)
    }
}

/** Fila etiqueta / valor del resumen; [emphasis] resalta el total. */
fun ViewPriceRowBinding.set(label: String, value: String, emphasis: Boolean = false) {
    tvLabel.text = label
    tvValue.text = value
    if (emphasis) {
        TextViewCompat.setTextAppearance(tvLabel, R.style.TextAppearance_Agro_SubtitleStrong)
        TextViewCompat.setTextAppearance(tvValue, R.style.TextAppearance_Agro_Price)
    }
}
