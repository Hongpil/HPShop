package com.philip.portfolioprogrammershop.api.request

import okhttp3.MultipartBody
import okhttp3.RequestBody


data class ProductRegistrationRequest(
        val token: String?,
        val userId: Long?,
        val name: String?,
        val description: String?,
        val price: Int?,
        val categoryId: Int?,
        val imageMultipartBody: MultipartBody.Part,
        val imageRequestBody: RequestBody?
) {

    val isNotValidName get() = name?.length !in 1..40
    val isNotValidDescription get() = description?.length !in 1..500
    val isNotValidPrice get() = price?.let { it < 1 } ?: false
    val isNotValidCategoryId get() = categoryId == null

}