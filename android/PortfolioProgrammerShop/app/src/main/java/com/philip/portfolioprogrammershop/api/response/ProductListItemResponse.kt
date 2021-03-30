package com.philip.portfolioprogrammershop.api.response

data class ProductListItemResponse(
    val id: Long,
    val name: String,
    val description: String,
    val price: Int,
    val user_id: Long,
    val path: String
)