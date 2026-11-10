package com.jmariodev.agrodigital.ui.admin.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.databinding.ItemApprovalBinding

/** Elemento pendiente de aprobación: un usuario vendedor o un producto. */
data class Approval(
    val id: String,
    val title: String,
    val subtitle: String,
    val isProduct: Boolean
)

class ApprovalAdapter(
    private val onApprove: (Approval) -> Unit,
    private val onReject: (Approval) -> Unit
) : ListAdapter<Approval, ApprovalAdapter.Holder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemApprovalBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(getItem(position))

    inner class Holder(private val binding: ItemApprovalBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Approval) = with(binding) {
            val context = root.context
            tvTitle.text = item.title
            tvSubtitle.text = item.subtitle
            ivIcon.setImageResource(if (item.isProduct) R.drawable.ic_box else R.drawable.ic_users)
            val tint = if (item.isProduct) R.color.agro_primary else R.color.agro_secondary
            val background = if (item.isProduct) R.color.agro_primary_light else R.color.agro_secondary_light
            ivIcon.imageTintList = ContextCompat.getColorStateList(context, tint)
            ivIcon.backgroundTintList = ContextCompat.getColorStateList(context, background)
            btnApprove.setOnClickListener { onApprove(item) }
            btnReject.setOnClickListener { onReject(item) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<Approval>() {
        override fun areItemsTheSame(oldItem: Approval, newItem: Approval) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Approval, newItem: Approval) = oldItem == newItem
    }
}
