package com.philip.portfolioprogrammershop.signin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.philip.portfolioprogrammershop.R
import com.philip.portfolioprogrammershop.product.ProductMainActivity
import com.philip.portfolioprogrammershop.signup.SignupActivity
import com.philip.portfolioprogrammershop.util.EventObserver
import kotlinx.android.synthetic.main.activity_signin.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SigninActivity : AppCompatActivity(), View.OnClickListener {

    private val viewModel: SigninViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        // "에러 토스트"
        viewModel.showErrorToast.observe(this, EventObserver {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
        })
        // "이메일 형식 에러"
        viewModel.showEmailIncorrectToast.observe(this, EventObserver {
            Toast.makeText(this, R.string.email_error, Toast.LENGTH_SHORT).show()
        })
        // "비밀번호 형식 에러"
        viewModel.showPasswordIncorrectToast.observe(this, EventObserver {
            Toast.makeText(this, R.string.password_error, Toast.LENGTH_SHORT).show()
            edt_password.text = null
        })
        // "로그인 성공"
        viewModel.showSigninSuccessToast.observe(this, EventObserver {
            Toast.makeText(this, R.string.signin_success, Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@SigninActivity, ProductMainActivity::class.java))
        })
        // "로그인 실패"
        viewModel.showSigninFailedToast.observe(this, EventObserver {
            Toast.makeText(this, R.string.signin_failed, Toast.LENGTH_SHORT).show()
            edt_password.text = null
        })
        // "등록되지 않는 이메일 주소"
        viewModel.showNoUserToast.observe(this, EventObserver {
            Toast.makeText(this, R.string.signin_no_user, Toast.LENGTH_SHORT).show()
            edt_email.text = null
            edt_password.text = null
        })

    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.btn_login -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.signin(edt_email.text.toString(), edt_password.text.toString())
                }
            }
            R.id.btn_signup -> {
                val intent = Intent(this, SignupActivity::class.java)
                startActivityForResult(intent, SignupActivity.REQUEST_REGISTER_EMAIL)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                SignupActivity.REQUEST_REGISTER_EMAIL -> {
                    data?.let { edt_email.setText(data.getStringExtra("userEmail").toString()) }
                }
            }
        }
    }
}