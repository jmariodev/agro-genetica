package com.jmariodev.agrodigital.ui.common.orders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.data.model.Order
import com.jmariodev.agrodigital.databinding.ItemOrderBinding
import com.jmariodev.agrodigital.ui.common.setOrderStatus
import com.jmariodev.agrodigital.ui.common.toCop

/** Pedidos. [sellerView] muestra el comprador como título (pedidos recibidos). */
class OrderAdapter(
    private val sellerView: Boolean,
    private val onClick: (Order) -> Unit
) : ListAdapter<Order, OrderAdapter.Holder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(getItem(position))

    inner class Holder(private val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: Order) = with(binding) {
            val context = root.context
            if (sellerView) {
                tvTitle.text = order.buyerName
                tvSubtitle.text = context.getString(R.string.order_code_date, order.code, order.date)
                val first = order.items.first()
                tvItems.text = context.getString(R.string.order_first_item, first.productName, first.quantity)
            } else {
                tvTitle.text = order.code
                tvSubtitle.text = order.date
                tvItems.text = context.resources.getQuantityString(R.plurals.products_count, order.itemCount, order.itemCount)
            }
            tvTotal.text = order.total.toCop()
            tvStatus.setOrderStatus(order.status)
            root.setOnClickListener { onClick(order) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Order, newItem: Order) = oldItem == newItem
    }
}
