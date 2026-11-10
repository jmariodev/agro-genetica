package com.jmariodev.agrodigital.ui.buyer.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.data.model.CartItem
import com.jmariodev.agrodigital.databinding.ItemCartBinding
import com.jmariodev.agrodigital.ui.common.label
import com.jmariodev.agrodigital.ui.common.tintCoat
import com.jmariodev.agrodigital.ui.common.toCop

class CartAdapter(
    private val onIncrease: (CartItem) -> Unit,
    private val onDecrease: (CartItem) -> Unit,
    private val onRemove: (CartItem) -> Unit
) : ListAdapter<CartItem, CartAdapter.Holder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(getItem(position))

    inner class Holder(private val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CartItem) = with(binding) {
            val context = root.context
            val product = item.product
            tvName.text = product.name
            tvMeta.text = context.getString(R.string.item_type_breed, product.breed, context.getString(product.type.label()))
            tvPrice.text = product.price.toCop()
            tvQuantity.text = item.quantity.toString()
            ivProduct.tintCoat(product.darkCoat)
            btnMinus.isEnabled = item.quantity > 1
            btnPlus.isEnabled = item.quantity < product.stock
            btnMinus.setOnClickListener { onDecrease(item) }
            btnPlus.setOnClickListener { onIncrease(item) }
            btnDelete.setOnClickListener { onRemove(item) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem) = oldItem.product.id == newItem.product.id
        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem) = oldItem == newItem
    }
}
