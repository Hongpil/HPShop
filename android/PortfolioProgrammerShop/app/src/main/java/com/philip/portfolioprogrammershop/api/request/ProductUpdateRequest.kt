package com.philip.portfolioprogrammershop.api.request

import okhttp3.MultipartBody
import okhttp3.RequestBody

data class ProductUpdateRequest (
        val token: String?,
        val userId: Long?,
        val product_id: Long?,
        val product_name: String?,
        val product_description: String?,
        val product_price: Int?,
        val imageMultipartBody: MultipartBody.Part,
        val imageRequestBody: RequestBody?
) {

        val isNotValidName get() = product_name?.length !in 1..40
        val isNotValidDescription get() = product_description?.length !in 1..500
        val isNotValidPrice get() = product_price?.let { it < 1 } ?: false

}