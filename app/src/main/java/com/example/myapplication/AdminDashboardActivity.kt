package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ActivityAdminDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var userList: ArrayList<UserModel>
    private lateinit var adapter: AdminUserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        userList = ArrayList()

        setupRecyclerView()
        loadUsers()

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = AdminUserAdapter(userList,
            onDeleteClick = { user ->
                showDeleteDialog(user)
            },
            onBlockClick = { user ->
                toggleBlockUser(user)
            }
        )

        binding.recyclerUsers.layoutManager = LinearLayoutManager(this)
        binding.recyclerUsers.adapter = adapter
    }

    private fun loadUsers() {
        database.child("Users")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    userList.clear()

                    for (snap in snapshot.children) {
                        val user = snap.getValue(UserModel::class.java)
                        if (user != null) {
                            user.uid = snap.key ?: ""
                            userList.add(user)
                        }
                    }

                    binding.tvTotalUsers.text = "Total Users: ${userList.size}"
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@AdminDashboardActivity,
                        "Failed to load users",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun showDeleteDialog(user: UserModel) {
        AlertDialog.Builder(this)
            .setTitle("Delete User")
            .setMessage("Are you sure you want to delete ${user.name}?")
            .setPositiveButton("Yes") { _, _ ->
                deleteUser(user)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteUser(user: UserModel) {
        database.child("Users")
            .child(user.uid)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "User Deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Delete Failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun toggleBlockUser(user: UserModel) {

        val newStatus =
            if (user.status == "blocked") "active" else "blocked"

        database.child("Users")
            .child(user.uid)
            .child("status")
            .setValue(newStatus)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "User status updated",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Failed to update status",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}