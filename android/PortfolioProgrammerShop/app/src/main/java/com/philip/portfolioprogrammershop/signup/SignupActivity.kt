package com.philip.portfolioprogrammershop.signup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.philip.portfolioprogrammershop.R
import com.philip.portfolioprogrammershop.util.EventObserver
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity(), View.OnClickListener {

    private val viewModel: SignupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // "에러 토스트"
        viewModel.showErrorToast.observe(this, EventObserver {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
        })
        // "이메일 형식 에러"
        viewModel.showEmailIncorrectToast.observe(this, EventObserver {
            Toast.makeText(this, R.string.email_error, Toast.LENGTH_SHORT).show()
        })
        // "이메일 중복 알림"
        viewModel.showEmailOverlapToast.observe(this, EventObserver {
            Toast.makeText(this, R.string.email_overlap, Toast.LENGTH_SHORT).show()
            edit_email.text = null
        })
        // "비밀번호 형식 에러"
        viewModel.showPasswordIncorrectToast.observe(this, EventObserver {
            Toast.makeText(this, R.string.password_error, Toast.LENGTH_SHORT).show()
        })
        // "이름 형식 에러"
        viewModel.showNameIncorrectToast.observe(this, EventObserver {
            Toast.makeText(this, R.string.name_error, Toast.LENGTH_SHORT).show()
        })
        // "회원가입 성공"
        viewModel.showSignupSuccessToast.observe(this, EventObserver {
            Toast.makeText(this, R.string.signup_success, Toast.LENGTH_SHORT).show()
            val intent = Intent()
            intent.putExtra("userEmail", edit_email.text.toString())
            setResult(Activity.RESULT_OK, intent)
            finish()
        })
        // "이메일 전송 성공"
        viewModel.sendingEmailSuccess.observe(this, EventObserver {
            Toast.makeText(this, R.string.email_sending_success, Toast.LENGTH_SHORT).show()
            btn_email_auth.isEnabled = false
            btn_email_auth.setBackgroundResource(R.drawable.bag_btn_gray)
        })
        // "이메일 미인증"
        viewModel.emailAuthNotYet.observe(this, EventObserver {
            Toast.makeText(this, R.string.email_auth_not_yet, Toast.LENGTH_SHORT).show()
        })

    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.btn_email_auth -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.emailAuth("from_android_app", edit_email.text.toString())
                }
            }
            R.id.btn_store -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.signup("from_android_app", edit_email.text.toString(), edit_nickname.text.toString(), edit_password.text.toString())
                }
            }
        }
    }

    companion object {
        const val REQUEST_REGISTER_EMAIL = 0
    }

}