package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdminUserAdapter(
    private val userList: List<UserModel>,
    private val onDeleteClick: (UserModel) -> Unit,
    private val onBlockClick: (UserModel) -> Unit
) : RecyclerView.Adapter<AdminUserAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        val tvRole: TextView = itemView.findViewById(R.id.tvRole)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
        val btnBlock: Button = itemView.findViewById(R.id.btnBlock)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_admin, parent, false)

        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {

        val user = userList[position]

        holder.tvName.text = "Name: ${user.name}"
        holder.tvEmail.text = "Email: ${user.email}"
        holder.tvRole.text = "Role: ${user.role}"
        holder.tvStatus.text = "Status: ${user.status}"

        // Change button text based on status
        if (user.status == "blocked") {
            holder.btnBlock.text = "Unblock"
        } else {
            holder.btnBlock.text = "Block"
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(user)
        }

        holder.btnBlock.setOnClickListener {
            onBlockClick(user)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}