package com.jmariodev.agrodigital.ui.common.product

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.data.model.Product
import com.jmariodev.agrodigital.databinding.ItemProductGridBinding
import com.jmariodev.agrodigital.ui.common.tintCoat
import com.jmariodev.agrodigital.ui.common.toCop
import com.jmariodev.agrodigital.ui.common.visibleIf

/**
 * Tarjetas de producto del catálogo. [horizontal] fija el ancho para los carruseles del inicio.
 */
class ProductGridAdapter(
    private val horizontal: Boolean = false,
    private val onClick: (Product) -> Unit,
    private val onAddToCart: (Product) -> Unit
) : ListAdapter<Product, ProductGridAdapter.Holder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemProductGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        if (horizontal) {
            binding.root.layoutParams = binding.root.layoutParams.apply {
                width = parent.resources.getDimensionPixelSize(R.dimen.product_card_width)
            }
        }
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(getItem(position))

    inner class Holder(private val binding: ItemProductGridBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) = with(binding) {
            val context = root.context
            tvName.text = product.name
            tvRating.text = context.getString(R.string.rating_format, product.rating)
            tvMeta.text = context.getString(R.string.product_meta, product.breed, product.abv)
            ivProduct.tintCoat(product.darkCoat)

            val live = product.isLiveAnimal
            tvBadge.visibleIf(live || product.premium)
            tvBadge.setText(if (live) R.string.badge_live else R.string.badge_premium)
            tvPrice.text = if (live) context.getString(R.string.price_negotiable) else product.price.toCop()
            btnAddCart.visibleIf(!live)

            root.setOnClickListener { onClick(product) }
            btnAddCart.setOnClickListener { onAddToCart(product) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Product, newItem: Product) = oldItem == newItem
    }
}
