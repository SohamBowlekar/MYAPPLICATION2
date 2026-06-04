package com.example.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemSelectedProductBinding

class SelectedProductAdapter(
    private val productList: List<Product>
) : RecyclerView.Adapter<SelectedProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(val binding: ItemSelectedProductBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {

        val binding = ItemSelectedProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {

        val product = productList[position]

        holder.binding.txtProductName.text = product.name
        holder.binding.txtProductQty.text = "Qty: ${product.quantity}"
        holder.binding.txtProductPrice.text = "Price: ₹${product.price}"

        val total = product.price * product.quantity
        holder.binding.txtProductTotal.text = "Total: ₹$total"
    }

    override fun getItemCount(): Int {
        return productList.size
    }
}