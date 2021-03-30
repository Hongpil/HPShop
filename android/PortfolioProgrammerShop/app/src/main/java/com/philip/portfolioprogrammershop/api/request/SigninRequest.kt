package com.philip.portfolioprogrammershop.api.request

import android.util.Patterns

class SigninRequest(
        val login_email: String?,
        val login_password: String?
) {
    fun isNotValidEmail() =
            login_email.isNullOrBlank()
                    || !Patterns.EMAIL_ADDRESS.matcher(login_email).matches()

    fun isNotValidPassword() =
            login_password.isNullOrBlank() || login_password.length !in 8..20
}