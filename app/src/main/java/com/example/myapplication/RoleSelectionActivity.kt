package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityRoleSelectionBinding

class RoleSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoleSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoleSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAdmin.setOnClickListener {
            goToRegister("Admin")
        }

        binding.btnMainDistributor.setOnClickListener {
            goToRegister("Main Distributor")
        }

        binding.btnSubDistributor.setOnClickListener {
            goToRegister("Sub Distributor")
        }

        binding.btnShopkeeper.setOnClickListener {
            goToRegister("Shopkeeper")
        }
    }

    private fun goToRegister(role: String) {
        val intent = Intent(this, RegisterActivity::class.java)
        intent.putExtra("USER_ROLE", role)
        startActivity(intent)
    }
}
