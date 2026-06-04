package com.example.myapplication

data class Order(
    var orderId: String = "",
    var buyerId: String = "",
    var buyerName: String = "",
    var buyerContact: String = "",
    var buyerAddress: String = "",
    var totalAmount: Int = 0,
    var status: String = "Pending"
)