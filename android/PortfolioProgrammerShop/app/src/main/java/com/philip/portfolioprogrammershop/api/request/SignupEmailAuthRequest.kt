package com.philip.portfolioprogrammershop.api.request

import android.util.Patterns

class SignupEmailAuthRequest(
        val email: String?
) {
    fun isNotValidEmail() =
            email.isNullOrBlank()
                    || !Patterns.EMAIL_ADDRESS.matcher(email).matches()
}