package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityShopkeeperBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ShopkeeperActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShopkeeperBinding
    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private val availableList = ArrayList<Product>()
    private val myCartList = ArrayList<Product>()

    private lateinit var availableAdapter: ShopkeeperAvailableProductAdapter
    private lateinit var myProductAdapter: MyProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopkeeperBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance(
            "https://fizzflow-d3a97-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).reference

        // AVAILABLE PRODUCTS
        availableAdapter = ShopkeeperAvailableProductAdapter(
            availableList
        ) { product, qty ->
            addToCartAndReduceStock(product, qty)
        }

        binding.rvAvailableProducts.layoutManager = LinearLayoutManager(this)
        binding.rvAvailableProducts.adapter = availableAdapter

        // MY CART
        myProductAdapter = MyProductAdapter(this, myCartList)
        binding.rvMyProducts.layoutManager = LinearLayoutManager(this)
        binding.rvMyProducts.adapter = myProductAdapter

        fetchSubDistributorProducts()
        fetchMyCart()

        // BUY
        binding.btnBuy.setOnClickListener {
            if (myCartList.isEmpty()) {
                Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            startActivity(
                Intent(this, BuyerDetailsActivity::class.java)
                    .putParcelableArrayListExtra("productList", ArrayList(myCartList))
            )
        }

        //  LOGOUT (FIXED)
        binding.btnLogout.setOnClickListener {
            auth.signOut()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            finish()
        }
    }

    // 🔹 FETCH AVAILABLE PRODUCTS
    private fun fetchSubDistributorProducts() {
        dbRef.child("sub_distributors")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    availableList.clear()
                    for (sub in snapshot.children) {
                        for (prod in sub.child("my_products").children) {
                            val p = prod.getValue(Product::class.java)
                            if (p != null && p.quantity > 0) {
                                availableList.add(p)
                            }
                        }
                    }
                    availableAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    //  ADD TO CART + REDUCE STOCK
    private fun addToCartAndReduceStock(product: Product, qty: Int) {
        val uid = auth.currentUser!!.uid

        val cartProduct = product.copy(
            quantity = qty,
            price = product.price * qty
        )

        val updates = hashMapOf<String, Any>(
            "/shopkeepers/$uid/my_products/${product.productId}" to cartProduct,
            "/sub_distributors/${product.distributorName}/my_products/${product.productId}/quantity"
                    to (product.quantity - qty)
        )

        dbRef.updateChildren(updates).addOnSuccessListener {
            Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show()
        }
    }

    //  FETCH CART
    private fun fetchMyCart() {
        val uid = auth.currentUser!!.uid
        dbRef.child("shopkeepers").child(uid).child("my_products")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    myCartList.clear()
                    for (snap in snapshot.children) {
                        snap.getValue(Product::class.java)?.let {
                            myCartList.add(it)
                        }
                    }
                    myProductAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
