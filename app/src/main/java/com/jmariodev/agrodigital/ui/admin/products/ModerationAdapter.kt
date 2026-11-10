package com.jmariodev.agrodigital.ui.admin.products

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.data.model.Product
import com.jmariodev.agrodigital.data.model.ProductStatus
import com.jmariodev.agrodigital.databinding.ItemProductModerationBinding
import com.jmariodev.agrodigital.ui.common.BadgeTone
import com.jmariodev.agrodigital.ui.common.label
import com.jmariodev.agrodigital.ui.common.setBadge
import com.jmariodev.agrodigital.ui.common.tintCoat
import com.jmariodev.agrodigital.ui.common.toCop
import com.jmariodev.agrodigital.ui.common.visibleIf

interface ModerationActions {
    fun onDetails(product: Product)
    fun onApprove(product: Product)
    fun onFlag(product: Product)
    fun onRemove(product: Product)
}

class ModerationAdapter(private val listener: ModerationActions) :
    ListAdapter<Product, ModerationAdapter.Holder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemProductModerationBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(getItem(position))

    inner class Holder(private val binding: ItemProductModerationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) = with(binding) {
            val context = root.context
            tvName.text = product.name
            tvType.setBadge(product.type.label(), BadgeTone.NEUTRAL)
            tvSeller.text = context.getString(R.string.moderation_seller, product.sellerName)
            tvPrice.text = if (product.isLiveAnimal) context.getString(R.string.price_negotiable) else product.price.toCop()
            tvDate.text = product.createdLabel
            ivProduct.tintCoat(product.darkCoat)

            val reported = product.status == ProductStatus.REPORTADO
            reportBox.visibleIf(reported)
            tvReason.text = context.getString(R.string.moderation_reason, product.reportReason.orEmpty())
            btnApprove.visibleIf(product.status == ProductStatus.PENDIENTE)
            btnFlag.visibleIf(!reported)

            btnDetails.setOnClickListener { listener.onDetails(product) }
            btnApprove.setOnClickListener { listener.onApprove(product) }
            btnFlag.setOnClickListener { listener.onFlag(product) }
            btnForceApprove.setOnClickListener { listener.onApprove(product) }
            btnRemove.setOnClickListener { listener.onRemove(product) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Product, newItem: Product) = oldItem == newItem
    }
}
