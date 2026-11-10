package com.jmariodev.agrodigital.ui.admin.users

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.data.model.User
import com.jmariodev.agrodigital.databinding.ItemPendingUserBinding
import com.jmariodev.agrodigital.databinding.ItemUserBinding
import com.jmariodev.agrodigital.ui.common.label
import com.jmariodev.agrodigital.ui.common.setUserRole
import com.jmariodev.agrodigital.ui.common.setUserStatus

private object UserDiff : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: User, newItem: User) = oldItem == newItem
}

/** Solicitudes de vendedores pendientes de aprobación. */
class PendingUserAdapter(
    private val onApprove: (User) -> Unit,
    private val onReject: (User) -> Unit
) : ListAdapter<User, PendingUserAdapter.Holder>(UserDiff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemPendingUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(getItem(position))

    inner class Holder(private val binding: ItemPendingUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) = with(binding) {
            val context = root.context
            tvName.text = user.name
            tvMeta.text = context.getString(R.string.user_role_location, context.getString(user.role.label()), user.location)
            tvTime.text = user.createdLabel
            btnApprove.setOnClickListener { onApprove(user) }
            btnReject.setOnClickListener { onReject(user) }
        }
    }
}

/** Usuarios registrados con menú de acciones. */
class UserAdapter(
    private val onClick: (User) -> Unit,
    private val onMore: (User, View) -> Unit
) : ListAdapter<User, UserAdapter.Holder>(UserDiff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(getItem(position))

    inner class Holder(private val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) = with(binding) {
            tvName.text = user.name
            tvEmail.text = user.email
            tvRole.setUserRole(user.role)
            tvStatus.setUserStatus(user.status)
            root.setOnClickListener { onClick(user) }
            btnMore.setOnClickListener { onMore(user, it) }
        }
    }
}
