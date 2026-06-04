package com.example.myapplication

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(

    var productId: String = "",
    var name: String = "",
    var distributorName: String = "",
    var category: String = "",
    var price: Int = 0,
    var quantity: Int = 0,
    var availableQty: Int = 0,
    var imageUri: String? = null

) : Parcelable