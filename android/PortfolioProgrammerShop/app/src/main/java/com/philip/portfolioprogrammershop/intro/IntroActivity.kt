package com.philip.portfolioprogrammershop.intro

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.philip.portfolioprogrammershop.R
import com.philip.portfolioprogrammershop.common.Prefs
import com.philip.portfolioprogrammershop.product.ProductMainActivity
import com.philip.portfolioprogrammershop.signin.SigninActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        GlobalScope.launch(Dispatchers.Main) {
            delay(1500)

            if(Prefs.token.isNullOrEmpty()) {
                startActivity(Intent(this@IntroActivity, SigninActivity::class.java))
            } else {
                startActivity(Intent(this@IntroActivity, ProductMainActivity::class.java))
            }

            finish()
        }
    }
}