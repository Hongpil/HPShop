package com.philip.portfolioprogrammershop.api.response

data class SigninResponse(
        val token: String,
        val userName: String,
        val userId: Long
)