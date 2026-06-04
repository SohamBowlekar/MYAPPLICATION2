package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityViewOrdersBinding
import com.google.firebase.database.*

class ViewOrdersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewOrdersBinding
    private lateinit var orderList: ArrayList<Order>
    private lateinit var adapter: OrderAdapter
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityViewOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        orderList = ArrayList()
        adapter = OrderAdapter(this, orderList)

        binding.rvOrders.layoutManager = LinearLayoutManager(this)
        binding.rvOrders.adapter = adapter

        dbRef = FirebaseDatabase.getInstance(
            "https://fizzflow-d3a97-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).reference.child("orders")

        loadOrders()
    }

    private fun loadOrders() {

        dbRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                orderList.clear()

                for (snap in snapshot.children) {

                    val order = snap.getValue(Order::class.java)

                    if (order != null) {
                        order.orderId = snap.key ?: ""
                        orderList.add(order)
                    }
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@ViewOrdersActivity,
                    error.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}