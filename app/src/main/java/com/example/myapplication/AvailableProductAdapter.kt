package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemAvailableProductBinding

class AvailableProductAdapter(
    private val context: Context,
    private val productList: ArrayList<Product>,
    private val onAddClick: (Product) -> Unit
) : RecyclerView.Adapter<AvailableProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(val binding: ItemAvailableProductBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        return ProductViewHolder(
            ItemAvailableProductBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {

        val product = productList[position]

        // ✅ FIX: Use quantity as stock if availableQty is 0
        val stock = if (product.availableQty > 0) product.availableQty else product.quantity

        // Default selected qty = 1
        if (product.quantity <= 0) {
            product.quantity = 1
        }

        // 🔹 Set values
        holder.binding.txtDistributorName.text =
            "Distributor: ${product.distributorName}"

        holder.binding.txtProductName.text = product.name

        holder.binding.txtPrice.text =
            "Price: ₹${product.price}"

        holder.binding.txtQty.text =
            product.quantity.toString()

        holder.binding.txtTotal.text =
            "Total: ₹${product.price * product.quantity}"

        holder.binding.txtAvailableStock.text =
            "Available: $stock"

        // ➕ PLUS BUTTON
        holder.binding.btnPlus.setOnClickListener {

            if (product.quantity < stock) {

                product.quantity++

                holder.binding.txtQty.text =
                    product.quantity.toString()

                holder.binding.txtTotal.text =
                    "Total: ₹${product.price * product.quantity}"

            } else {
                Toast.makeText(
                    context,
                    "Only $stock available",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // ➖ MINUS BUTTON
        holder.binding.btnMinus.setOnClickListener {

            if (product.quantity > 1) {

                product.quantity--

                holder.binding.txtQty.text =
                    product.quantity.toString()

                holder.binding.txtTotal.text =
                    "Total: ₹${product.price * product.quantity}"
            }
        }

        // 🛒 ADD TO CART BUTTON
        holder.binding.btnAddProduct.setOnClickListener {

            if (stock <= 0) {
                Toast.makeText(context, "Out of Stock", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            onAddClick(product)
        }
    }

    override fun getItemCount(): Int = productList.size
}