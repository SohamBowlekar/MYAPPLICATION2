package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemOrderBinding
import com.google.firebase.database.FirebaseDatabase

class OrderAdapter(
    private val context: Context,
    private val orderList: ArrayList<Order>
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(val binding: ItemOrderBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        return OrderViewHolder(
            ItemOrderBinding.inflate(LayoutInflater.from(context), parent, false)
        )
    }

    override fun getItemCount(): Int = orderList.size

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {

        val order = orderList[position]

        holder.binding.txtBuyerName.text = "Buyer: ${order.buyerName}"
        holder.binding.txtBuyerContact.text = "Contact: ${order.buyerContact}"
        holder.binding.txtBuyerAddress.text = "Address: ${order.buyerAddress}"
        holder.binding.txtTotal.text = "Total: ₹${order.totalAmount}"
        holder.binding.txtStatus.text = "Status: ${order.status}"

        // ✅ ACCEPT
        holder.binding.btnAccept.setOnClickListener {

            FirebaseDatabase.getInstance().reference
                .child("orders")
                .child(order.orderId)
                .child("status")
                .setValue("approved")

            Toast.makeText(context, "Order Approved", Toast.LENGTH_SHORT).show()
        }

        // ❌ REJECT
        holder.binding.btnReject.setOnClickListener {

            FirebaseDatabase.getInstance().reference
                .child("orders")
                .child(order.orderId)
                .child("status")
                .setValue("rejected")

            Toast.makeText(context, "Order Rejected", Toast.LENGTH_SHORT).show()
        }
    }
}