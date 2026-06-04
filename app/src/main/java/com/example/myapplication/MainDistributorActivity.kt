package com.example.myapplication

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*
import android.util.Log

class MainDistributorActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productList: ArrayList<Product>
    private lateinit var adapter: ProductAdapter
    private lateinit var spinnerCategory: Spinner



    private val dbRef = FirebaseDatabase.getInstance(
        "https://fizzflow-d3a97-default-rtdb.asia-southeast1.firebasedatabase.app"
    ).reference.child("products")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_distributor)

        recyclerView = findViewById(R.id.productRecyclerView)
        spinnerCategory = findViewById(R.id.spinnerCategory)

        productList = ArrayList()
        adapter = ProductAdapter(this, productList)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        setupCategorySpinner()
        loadProductsFromFirebase()

        findViewById<Button>(R.id.btnSelectImage).setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 101)
        }

        findViewById<Button>(R.id.btnAddProduct).setOnClickListener {
            addProduct()
        }

        findViewById<Button>(R.id.btnViewComplaints).setOnClickListener {
            startActivity(Intent(this, ComplaintsActivity::class.java))
        }

        findViewById<Button>(R.id.btnGlassBottleReturn).setOnClickListener {
            startActivity(Intent(this, GlassBottleReturnActivity::class.java))
        }

        //  View Orders (next step logic)
        findViewById<Button>(R.id.btnViewOrders).setOnClickListener {
            startActivity(Intent(this, ViewOrdersActivity::class.java))
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupCategorySpinner() {
        val categories = listOf(
            "Select Category",
            "Tin",
            "Glass Bottle",
            "Plastic Bottle",
            "Can",
            "Others"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )
        spinnerCategory.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data?.toString()

            val img = findViewById<ImageView>(R.id.imgPreview)

            imageUri?.let {
                img.setImageURI(Uri.parse(it))
                img.visibility = View.VISIBLE
            }
        }
    }
    private var imageUri: String? = null
    private fun addProduct() {

        val distributor = findViewById<EditText>(R.id.etDistributorName).text.toString().trim()
        val name = findViewById<EditText>(R.id.etProductName).text.toString().trim()
        val category = spinnerCategory.selectedItem.toString()
        val qty = findViewById<EditText>(R.id.etProductQty).text.toString().toIntOrNull()
        val price = findViewById<EditText>(R.id.etProductPrice).text.toString().toIntOrNull()

        if (distributor.isEmpty() || name.isEmpty()
            || category == "Select Category"
            || qty == null || price == null
        ) {
            Toast.makeText(this, "Fill all fields properly", Toast.LENGTH_SHORT).show()
            return
        }

        val productId = dbRef.push().key!!   // ✅ BETTER THAN UUID

        val product = Product(
            productId,
            name,
            distributor,
            category,
            price,
            qty,
            qty,
            imageUri ?: ""
        )

        dbRef.child(productId).setValue(product)
            .addOnSuccessListener {
                Toast.makeText(this, "Product Added", Toast.LENGTH_SHORT).show()
                clearInputs()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadProductsFromFirebase() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("FirebaseData", "Snapshot: ${snapshot.value}")

                productList.clear()

                for (snap in snapshot.children) {
                    val product = snap.getValue(Product::class.java)

                    if (product != null) {
                        product.productId = snap.key ?: ""
                        productList.add(product)
                    }
                }

                //  IMPORTANT DEBUG
                Toast.makeText(this@MainDistributorActivity,
                    "Loaded: ${productList.size} products",
                    Toast.LENGTH_SHORT
                ).show()

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainDistributorActivity, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun clearInputs() {
        findViewById<EditText>(R.id.etDistributorName).text.clear()
        findViewById<EditText>(R.id.etProductName).text.clear()
        findViewById<EditText>(R.id.etProductQty).text.clear()
        findViewById<EditText>(R.id.etProductPrice).text.clear()
        spinnerCategory.setSelection(0)
        findViewById<ImageView>(R.id.imgPreview).visibility = View.GONE
        imageUri = null
    }
    }
