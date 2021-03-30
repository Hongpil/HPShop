package com.philip.portfolioprogrammershop.product.detail

data class ProductInfo (
    val product_id: Long,
    val product_name: String,
    val product_description: String,
    val product_price: Int,
    val product_path: String
)