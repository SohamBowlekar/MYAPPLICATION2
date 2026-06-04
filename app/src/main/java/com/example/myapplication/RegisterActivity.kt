package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(
            "https://fizzflow-d3a97-default-rtdb.asia-southeast1.firebasedatabase.app/"
        )

        // Redirect if already logged in
        auth.currentUser?.uid?.let { checkUserRoleAndRedirect(it) }

        // Register buttons
        binding.btnAdmin.setOnClickListener { registerUser("Admin", "admins") }
        binding.btnMainDistributor.setOnClickListener { registerUser("Main Distributor", "main_distributors") }
        binding.btnSubDistributor.setOnClickListener { registerUser("Sub Distributor", "sub_distributors") }
        binding.btnShopkeeper.setOnClickListener { registerUser("Shopkeeper", "shopkeepers") }

        // Go to Login
        binding.btnGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser(role: String, node: String) {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener

                    val userData = mapOf(
                        "uid" to uid,
                        "name" to name,
                        "email" to email,
                        "role" to role,
                        "timestamp" to System.currentTimeMillis()
                    )

                    val dbRef = database.reference

                    // Save under /users/<role>/<uid>
                    dbRef.child("users").child(node).child(uid)
                        .setValue(userData)
                        .addOnSuccessListener {
                            // Save role map /user_roles/<uid> = role
                            dbRef.child("user_roles").child(uid).setValue(role)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Registered as $role", Toast.LENGTH_SHORT).show()
                                    goToDashboard(role)
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Error saving role: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "DB Write Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                        }

                } else {
                    Toast.makeText(this, "Auth Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkUserRoleAndRedirect(uid: String) {
        val ref = database.reference.child("user_roles").child(uid)
        ref.get().addOnSuccessListener {
            if (it.exists()) {
                val role = it.value.toString()
                goToDashboard(role)
            }
        }
    }

    private fun goToDashboard(role: String) {
        val next = when (role) {
            "Admin" -> AdminDashboardActivity::class.java
            "Main Distributor" -> MainDistributorActivity::class.java
            "Sub Distributor" -> SubDistributorActivity::class.java
            "Shopkeeper" -> ShopkeeperActivity::class.java
            else -> MainActivity::class.java
        }
        startActivity(Intent(this, next))
        finish()
    }
}
