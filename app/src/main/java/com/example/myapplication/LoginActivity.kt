package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(
            "https://fizzflow-d3a97-default-rtdb.asia-southeast1.firebasedatabase.app/"
        )

        // If already logged in, auto redirect
        auth.currentUser?.uid?.let { checkUserRoleAndRedirect(it) }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                        checkUserRoleAndRedirect(uid)
                    } else {
                        Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        binding.btnGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun checkUserRoleAndRedirect(uid: String) {
        val ref = database.reference.child("user_roles").child(uid)
        ref.get().addOnSuccessListener {
            if (it.exists()) {
                val role = it.value.toString()
                goToDashboard(role)
            } else {
                Toast.makeText(this, "No role found. Please re-register.", Toast.LENGTH_LONG).show()
                auth.signOut()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "DB Error: ${it.message}", Toast.LENGTH_SHORT).show()
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

        Toast.makeText(this, "Welcome $role!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, next)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
