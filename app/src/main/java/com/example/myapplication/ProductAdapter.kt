package com.example.myapplication

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemProductBinding
import com.google.firebase.database.FirebaseDatabase

class ProductAdapter(
    private val context: Context,
    private val products: ArrayList<Product>
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(val binding: ItemProductBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        return ProductViewHolder(
            ItemProductBinding.inflate(LayoutInflater.from(context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {

        val item = products[position]

        // ✅ TEXT BINDING
        holder.binding.txtProductName.text = item.name
        holder.binding.txtProductQty.text = "Qty: ${item.quantity}"
        holder.binding.txtProductPrice.text = "₹${item.price}"
        holder.binding.txtDistributorName.text =
            "${item.distributorName} • ${item.category}"

        // ✅ IMAGE FIX (IMPORTANT)
        if (!item.imageUri.isNullOrEmpty()) {
            try {
                holder.binding.imgProduct.setImageURI(Uri.parse(item.imageUri))
            } catch (e: Exception) {
                holder.binding.imgProduct.setImageResource(R.drawable.ic_image_placeholder)
            }
        } else {
            holder.binding.imgProduct.setImageResource(R.drawable.ic_image_placeholder)
        }

        // ✅ DELETE
        holder.binding.btnDelete.setOnClickListener {
            FirebaseDatabase.getInstance()
                .getReference("products")
                .child(item.productId)
                .removeValue()

            Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
        }

        // ✅ EDIT
        holder.binding.btnEdit.setOnClickListener {
            showEditDialog(item)
        }
    }

    override fun getItemCount(): Int = products.size

    private fun showEditDialog(item: Product) {

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Edit Product")

        val input = EditText(context)
        input.hint = "Enter new quantity"
        input.setText(item.quantity.toString()) // ✅ FIX

        builder.setView(input)

        builder.setPositiveButton("Update") { _, _ ->

            val newQty = input.text.toString().toIntOrNull()

            if (newQty != null) {

                FirebaseDatabase.getInstance()
                    .getReference("products")
                    .child(item.productId)
                    .child("quantity")
                    .setValue(newQty)

                Toast.makeText(context, "Updated", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }
}