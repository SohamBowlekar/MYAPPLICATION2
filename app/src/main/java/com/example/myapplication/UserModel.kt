package com.example.myapplication

data class UserModel(
    var uid: String = "",
    var name: String = "",
    var email: String = "",
    var role: String = "",
    var status: String = "active"
)