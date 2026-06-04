package com.example.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemShopkeeperAvailableProductBinding

class ShopkeeperAvailableProductAdapter(
    private val productList: ArrayList<Product>,
    private val onAddClick: (Product, Int) -> Unit
) : RecyclerView.Adapter<ShopkeeperAvailableProductAdapter.ProductViewHolder>() {

    private val quantityMap = HashMap<String, Int>()

    inner class ProductViewHolder(
        val binding: ItemShopkeeperAvailableProductBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemShopkeeperAvailableProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        val unitPrice = product.price
        val selectedQty = quantityMap[product.productId] ?: 1
        quantityMap[product.productId] = selectedQty

        // Product info
        holder.binding.txtProductName.text = product.name
        holder.binding.txtDistributorName.text =
            "From: ${product.distributorName}"

        holder.binding.txtProductQty.text = selectedQty.toString()
        holder.binding.txtProductPrice.text =
            "₹ ${unitPrice * selectedQty}"

        // ➕ PLUS
        holder.binding.btnPlus.setOnClickListener {
            val currentQty = quantityMap[product.productId] ?: 1
            if (currentQty < product.quantity) {
                val newQty = currentQty + 1
                quantityMap[product.productId] = newQty
                holder.binding.txtProductQty.text = newQty.toString()
                holder.binding.txtProductPrice.text =
                    "₹ ${unitPrice * newQty}"
            }
        }

        // ➖ MINUS
        holder.binding.btnMinus.setOnClickListener {
            val currentQty = quantityMap[product.productId] ?: 1
            if (currentQty > 1) {
                val newQty = currentQty - 1
                quantityMap[product.productId] = newQty
                holder.binding.txtProductQty.text = newQty.toString()
                holder.binding.txtProductPrice.text =
                    "₹ ${unitPrice * newQty}"
            }
        }

        // 🛒 ADD TO CART
        holder.binding.btnAdd.setOnClickListener {
            val qty = quantityMap[product.productId] ?: 1
            onAddClick(product, qty)
        }
    }

    override fun getItemCount(): Int = productList.size
}
