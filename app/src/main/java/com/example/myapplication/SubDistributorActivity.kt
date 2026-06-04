package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivitySubDistributorBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SubDistributorActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubDistributorBinding
    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private val availableList = ArrayList<Product>()
    private val myProductList = ArrayList<Product>()

    private lateinit var availableAdapter: AvailableProductAdapter
    private lateinit var myProductAdapter: MyProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubDistributorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance(
            "https://fizzflow-d3a97-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).reference

        binding.textViewTitle.text = "Available Products 🏬"

        //  AVAILABLE PRODUCTS
        availableAdapter =
            AvailableProductAdapter(this, availableList, ::addProductToMyList)

        binding.rvAvailableProducts.layoutManager = LinearLayoutManager(this)
        binding.rvAvailableProducts.adapter = availableAdapter

        //  MY PRODUCTS
        myProductAdapter = MyProductAdapter(this, myProductList)

        binding.rvMyProducts.layoutManager = LinearLayoutManager(this)
        binding.rvMyProducts.adapter = myProductAdapter

        fetchDistributorProducts()
        fetchMyProducts()

        binding.btnBuy.setOnClickListener {

            if (myProductList.isEmpty()) {
                Toast.makeText(this, "No products to buy", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, BuyerDetailsActivity::class.java)
            intent.putParcelableArrayListExtra(
                "productList",
                ArrayList(myProductList)
            )
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(
                Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            )
            finish()
        }
    }

    // 🔹 FETCH MAIN DISTRIBUTOR PRODUCTS
    private fun fetchDistributorProducts() {
        dbRef.child("products")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    availableList.clear()
                    for (snap in snapshot.children) {
                        val product = snap.getValue(Product::class.java)

                        if (product != null) {
                            //  Category automatically comes from Firebase
                            availableList.add(product)
                        }
                    }
                    availableAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // 🔹 ADD PRODUCT TO SUB DISTRIBUTOR
    private fun addProductToMyList(product: Product) {

        val uid = auth.currentUser!!.uid

        if (product.quantity <= 0) {
            Toast.makeText(this, "Quantity must be at least 1", Toast.LENGTH_SHORT).show()
            return
        }

        dbRef.child("sub_distributors")
            .child(uid)
            .child("my_products")
            .child(product.productId)
            .setValue(product)  //  Full product saved including category
            .addOnSuccessListener {
                Toast.makeText(this, "Product added", Toast.LENGTH_SHORT).show()
            }
    }

    //  FETCH SUB DISTRIBUTOR PRODUCTS
    private fun fetchMyProducts() {

        val uid = auth.currentUser!!.uid

        dbRef.child("sub_distributors")
            .child(uid)
            .child("my_products")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    myProductList.clear()

                    for (snap in snapshot.children) {
                        val product = snap.getValue(Product::class.java)

                        if (product != null) {
                            // ✅ Category also loaded here
                            myProductList.add(product)
                        }
                    }

                    myProductAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    //  PLACE ORDER
    fun placeOrder(
        buyerName: String,
        buyerContact: String,
        buyerAddress: String
    ) {

        if (myProductList.isEmpty()) {
            Toast.makeText(this, "No products to buy", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = auth.currentUser!!.uid
        val orderRef = dbRef.child("orders").push()

        val buyCode = "BUY-${(1000..9999).random()}"
        var totalAmount = 0

        val productMap = HashMap<String, Any>()

        for (product in myProductList) {

            val itemTotal = product.price * product.quantity
            totalAmount += itemTotal

            productMap[product.productId] = mapOf(
                "name" to product.name,
                "category" to product.category,   // ✅ CATEGORY INCLUDED
                "qty" to product.quantity,
                "price" to product.price,
                "total" to itemTotal
            )
        }

        val orderData = mapOf(
            "buyCode" to buyCode,
            "subDistributorId" to uid,
            "buyerName" to buyerName,
            "buyerContact" to buyerContact,
            "buyerAddress" to buyerAddress,
            "totalAmount" to totalAmount,
            "products" to productMap,
            "timestamp" to System.currentTimeMillis()
        )

        orderRef.setValue(orderData).addOnSuccessListener {
            Toast.makeText(
                this,
                "Order placed successfully\nBuy Code: $buyCode",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}