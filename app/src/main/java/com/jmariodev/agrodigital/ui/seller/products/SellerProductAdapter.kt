package com.jmariodev.agrodigital.ui.seller.products

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.data.model.Product
import com.jmariodev.agrodigital.databinding.ItemProductSellerBinding
import com.jmariodev.agrodigital.ui.common.label
import com.jmariodev.agrodigital.ui.common.setProductStatus
import com.jmariodev.agrodigital.ui.common.tintCoat
import com.jmariodev.agrodigital.ui.common.toCop

class SellerProductAdapter(
    private val onClick: (Product) -> Unit,
    private val onMore: (Product, View) -> Unit
) : ListAdapter<Product, SellerProductAdapter.Holder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemProductSellerBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(getItem(position))

    inner class Holder(private val binding: ItemProductSellerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) = with(binding) {
            val context = root.context
            tvName.text = product.name
            tvMeta.text = context.getString(R.string.seller_product_meta, context.getString(product.type.label()), product.breed, product.stock)
            tvPrice.text = if (product.isLiveAnimal) context.getString(R.string.price_negotiable) else product.price.toCop()
            tvStatus.setProductStatus(product.status)
            ivProduct.tintCoat(product.darkCoat)
            root.setOnClickListener { onClick(product) }
            btnMore.setOnClickListener { onMore(product, it) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Product, newItem: Product) = oldItem == newItem
    }
}
