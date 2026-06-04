package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemMyProductBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MyProductAdapter(
    private val context: Context,
    private val productList: ArrayList<Product>
) : RecyclerView.Adapter<MyProductAdapter.ProductViewHolder>() {

    private val auth = FirebaseAuth.getInstance()
    private val dbRef = FirebaseDatabase.getInstance(
        "https://fizzflow-d3a97-default-rtdb.asia-southeast1.firebasedatabase.app/"
    ).reference

    inner class ProductViewHolder(val binding: ItemMyProductBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemMyProductBinding.inflate(
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
        holder.binding.txtProductPrice.text = "₹ ${product.price}"
        holder.binding.txtProductTotal.text =
            "Total: ₹ ${product.price * product.quantity}"

        // ➕ Increase Quantity
        holder.binding.btnPlus.setOnClickListener {

            product.quantity += 1

            holder.binding.txtProductQty.text = "Qty: ${product.quantity}"
            holder.binding.txtProductTotal.text =
                "Total: ₹ ${product.price * product.quantity}"

            updateQuantityInFirebase(product)
        }

        // ➖ Decrease Quantity
        holder.binding.btnMinus.setOnClickListener {

            if (product.quantity > 1) {

                product.quantity -= 1

                holder.binding.txtProductQty.text = "Qty: ${product.quantity}"
                holder.binding.txtProductTotal.text =
                    "Total: ₹ ${product.price * product.quantity}"

                updateQuantityInFirebase(product)
            }
        }

        // ❌ REMOVE PRODUCT (FIXED)
        holder.binding.btnRemove.setOnClickListener {

            val currentPosition = holder.adapterPosition

            if (currentPosition == RecyclerView.NO_POSITION) return@setOnClickListener

            val uid = auth.currentUser?.uid ?: return@setOnClickListener

            dbRef.child("shopkeepers")
                .child(uid)
                .child("my_products")
                .child(product.productId)
                .removeValue()
                .addOnSuccessListener {

                    // 🔥 REMOVE FROM LIST (IMPORTANT FIX)
                    productList.removeAt(currentPosition)
                    notifyItemRemoved(currentPosition)
                    notifyItemRangeChanged(currentPosition, productList.size)

                    Toast.makeText(context, "Product removed", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to remove", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun getItemCount(): Int = productList.size

    // 🔄 UPDATE QUANTITY IN FIREBASE
    private fun updateQuantityInFirebase(product: Product) {
        val uid = auth.currentUser?.uid ?: return

        dbRef.child("shopkeepers")
            .child(uid)
            .child("my_products")
            .child(product.productId)
            .child("quantity")
            .setValue(product.quantity)
    }
}