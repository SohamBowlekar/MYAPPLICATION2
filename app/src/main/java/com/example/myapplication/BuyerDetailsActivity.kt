package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityBuyerDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class BuyerDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBuyerDetailsBinding
    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private lateinit var productList: ArrayList<Product>
    private var totalAmount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBuyerDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        dbRef = FirebaseDatabase.getInstance(
            "https://fizzflow-d3a97-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).reference

        // 🔹 Get product list from previous screen
        productList = intent.getParcelableArrayListExtra("productList") ?: arrayListOf()

        if (productList.isEmpty()) {
            Toast.makeText(this, "No products received", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupRecyclerView()

        calculateTotalPrice()

        // 🔹 Confirm Order Button
        binding.btnConfirmOrder.setOnClickListener {

            val name = binding.edtBuyerName.text.toString().trim()
            val contact = binding.edtBuyerContact.text.toString().trim()
            val address = binding.edtBuyerAddress.text.toString().trim()

            if (name.isEmpty() || contact.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            placeOrder(
                buyerName = name,
                buyerContact = contact,
                buyerAddress = address,
                products = productList
            )
        }
    }

    // 🔹 SETUP PRODUCT LIST
    private fun setupRecyclerView() {

        binding.rvSelectedProducts.layoutManager = LinearLayoutManager(this)

        val adapter = SelectedProductAdapter(productList)

        binding.rvSelectedProducts.adapter = adapter
    }

    // 🔹 CALCULATE TOTAL PRICE
    private fun calculateTotalPrice() {

        totalAmount = 0

        for (product in productList) {
            totalAmount += product.price * product.quantity
        }

        binding.txtTotalPrice.text = "Total Price: ₹$totalAmount"
    }

    // 🔹 PLACE ORDER → STORES BUYER + PRODUCTS
    private fun placeOrder(
        buyerName: String,
        buyerContact: String,
        buyerAddress: String,
        products: ArrayList<Product>
    ) {

        val uid = auth.currentUser?.uid ?: return

        val orderRef = dbRef.child("orders").push()

        val buyCode = "BUY-${(1000..9999).random()}"

        val productMap = HashMap<String, Any>()

        for (product in products) {

            val itemTotal = product.price * product.quantity

            productMap[product.productId] = mapOf(
                "name" to product.name,
                "quantity" to product.quantity,
                "price" to product.price,
                "total" to itemTotal,
                "distributorName" to product.distributorName
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

        orderRef.setValue(orderData)
            .addOnSuccessListener {

                Toast.makeText(
                    this,
                    "✅ Order Placed Successfully\nBuy Code: $buyCode",
                    Toast.LENGTH_LONG
                ).show()

                finish()
            }
            .addOnFailureListener {

                Toast.makeText(
                    this,
                    "Order failed. Try again",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}