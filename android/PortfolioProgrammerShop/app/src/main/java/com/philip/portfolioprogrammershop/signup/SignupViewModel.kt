package com.philip.portfolioprogrammershop.signup

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.philip.portfolioprogrammershop.api.ParayoApi
import com.philip.portfolioprogrammershop.api.request.SignupEmailAuthRequest
import com.philip.portfolioprogrammershop.api.request.SignupRequest
import com.philip.portfolioprogrammershop.api.response.ApiResponse
import com.philip.portfolioprogrammershop.util.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SignupViewModel : ViewModel() {


    // "에러 토스트"
    private val _showErrorToast = MutableLiveData<Event<Boolean>>()
    val showErrorToast: LiveData<Event<Boolean>> = _showErrorToast
    private fun onErrorToastShow() {
        _showErrorToast.postValue(Event(true))
    }

    // "이메일 형식 에러"
    private val _showEmailIncorrectToast = MutableLiveData<Event<Boolean>>()
    val showEmailIncorrectToast: LiveData<Event<Boolean>> = _showEmailIncorrectToast
    private fun onEmailIncorrectToastShow() {
        _showEmailIncorrectToast.postValue(Event(true))
    }

    // "이메일 중복 알림"
    private val _showEmailOverlapToast = MutableLiveData<Event<Boolean>>()
    val showEmailOverlapToast: LiveData<Event<Boolean>> = _showEmailOverlapToast
    private fun onEmailOverlapToastShow() {
        _showEmailOverlapToast.postValue(Event(true))
    }

    // "비밀번호 형식 에러"
    private val _showPasswordIncorrectToast = MutableLiveData<Event<Boolean>>()
    val showPasswordIncorrectToast: LiveData<Event<Boolean>> = _showPasswordIncorrectToast
    private fun onPasswordIncorrectToastShow() {
        _showPasswordIncorrectToast.postValue(Event(true))
    }

    // "이름 형식 에러"
    private val _showNameIncorrectToast = MutableLiveData<Event<Boolean>>()
    val showNameIncorrectToast: LiveData<Event<Boolean>> = _showNameIncorrectToast
    private fun onNameIncorrectToastShow() {
        _showNameIncorrectToast.postValue(Event(true))
    }

    // "회원가입 성공"
    private val _showSignupSuccessToast = MutableLiveData<Event<Boolean>>()
    val showSignupSuccessToast: LiveData<Event<Boolean>> = _showSignupSuccessToast
    private fun onSignupSuccessToastShow() {
        _showSignupSuccessToast.postValue(Event(true))
    }

    // "이메일 전송 성공"
    private val _sendingEmailSuccess = MutableLiveData<Event<Boolean>>()
    val sendingEmailSuccess: LiveData<Event<Boolean>> = _sendingEmailSuccess
    private fun onEmailSendingSuccess () {
        _sendingEmailSuccess.postValue(Event(true))
    }

    // "이메일 미인증"
    private val _emailAuthNotYet = MutableLiveData<Event<Boolean>>()
    val emailAuthNotYet: LiveData<Event<Boolean>> = _emailAuthNotYet
    private fun onEmailAuthNotYet () {
        _emailAuthNotYet.postValue(Event(true))
    }


    /**
     * 이메일 인증 요청
     */
    suspend fun emailAuth(header: String, email: String) {
        // 유효 검사
        val request = SignupEmailAuthRequest(email)
        if (isNotValidSignupEmailAuth(request)) {
            return
        }
        try {
            val response = requestSignupEmailAuth(header, email)
            onSignupEmailAuthResponse(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "error message : $e")
            onErrorToastShow()
        }
    }
    private suspend fun requestSignupEmailAuth(header: String, email: String) =
            withContext(Dispatchers.IO) {
                ParayoApi.instance.EmailAuth_Request(header, email)
            }
    private fun isNotValidSignupEmailAuth(signupEmailAuthRequest: SignupEmailAuthRequest) =
            when {
                signupEmailAuthRequest.isNotValidEmail() -> {
                    onEmailIncorrectToastShow()
                    true
                }
                else -> false
            }
    private fun onSignupEmailAuthResponse(response: ApiResponse<Void>) {
        if (response.success) {
            if (response.message == "email sending success") {
                onEmailSendingSuccess()
            } else {
                Log.e(TAG, "error message : ${response.message}")
                if (response.message == "duplicate email") {
                    onEmailOverlapToastShow()
                } else {
                    onErrorToastShow()
                }
            }
        }
    }


    /**
     * 이메일 인증 완료 후, 회원가입 요청
     */
    suspend fun signup(header: String, email: String, password: String, name: String) {
        // 유효 검사
        val request = SignupRequest(email, password, name)
        if (isNotValidSignup(request)) {
            return
        }
        try {
            val response = requestSignup(header, email, password, name)
            onSignupResponse(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "error message : $e")
            onErrorToastShow()
        }
    }
    private suspend fun requestSignup(header: String, email: String, password: String, name: String) =
            withContext(Dispatchers.IO) {
                ParayoApi.instance.UserJoin_Request(header, email, password, name)
            }
    private fun isNotValidSignup(signupRequest: SignupRequest) =
            when {
                signupRequest.isNotValidEmail() -> {
                    onEmailIncorrectToastShow()
                    true
                }
                signupRequest.isNotValidName() -> {
                    onNameIncorrectToastShow()
                    true
                }
                signupRequest.isNotValidPassword() -> {
                    onPasswordIncorrectToastShow()
                    true
                }
                else -> false
            }
    private fun onSignupResponse(response: ApiResponse<String>) {
        if (response.success && response.message == "user added") {
            Log.d(TAG, "success message data : ${response.data?.javaClass}")
            onSignupSuccessToastShow()
        } else {
            Log.e(TAG, "error message : ${response.message}")
            if (response.message == "email auth not yet") {
                onEmailAuthNotYet()
            } else {
                onErrorToastShow()
            }
        }
    }

    companion object {
        private const val TAG = "SignupViewModel"
    }

}