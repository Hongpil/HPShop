package com.philip.portfolioprogrammershop.signin

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.philip.portfolioprogrammershop.api.ParayoApi
import com.philip.portfolioprogrammershop.api.request.SigninRequest
import com.philip.portfolioprogrammershop.api.response.ApiResponse
import com.philip.portfolioprogrammershop.api.response.SigninResponse
import com.philip.portfolioprogrammershop.common.Prefs
import com.philip.portfolioprogrammershop.util.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SigninViewModel : ViewModel() {



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

    // "비밀번호 형식 에러"
    private val _showPasswordIncorrectToast = MutableLiveData<Event<Boolean>>()
    val showPasswordIncorrectToast: LiveData<Event<Boolean>> = _showPasswordIncorrectToast
    private fun onPasswordIncorrectToastShow() {
        _showPasswordIncorrectToast.postValue(Event(true))
    }

    // "로그인 성공"
    private val _showSigninSuccessToast = MutableLiveData<Event<Boolean>>()
    val showSigninSuccessToast: LiveData<Event<Boolean>> = _showSigninSuccessToast
    private fun onSigninSuccessToastShow() {
        _showSigninSuccessToast.postValue(Event(true))
    }

    // "로그인 실패"
    private val _showSigninFailedToast = MutableLiveData<Event<Boolean>>()
    val showSigninFailedToast: LiveData<Event<Boolean>> = _showSigninFailedToast
    private fun onSigninFailedToastShow() {
        _showSigninFailedToast.postValue(Event(true))
    }

    // "등록되지 않는 이메일 주소"
    private val _showNoUserToast = MutableLiveData<Event<Boolean>>()
    val showNoUserToast: LiveData<Event<Boolean>> = _showNoUserToast
    private fun onNoUserToastShow() {
        _showNoUserToast.postValue(Event(true))
    }

    suspend fun signin(email: String, password: String) {
        val request = SigninRequest(email, password)
        if(isNotValidSignin(request))
            return
        try {
            val response = requestSignin(request)
            onSigninResponse(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("SigninViewModel_tag", "error message : $e")
            onErrorToastShow()
        }
    }

    private fun isNotValidSignin(request: SigninRequest) =
            when {
                request.isNotValidEmail() -> {
                    onEmailIncorrectToastShow()
                    true
                }
                request.isNotValidPassword() -> {
                    onPasswordIncorrectToastShow()
                    true
                }
                else -> false
            }

    private suspend fun requestSignin(request: SigninRequest) =
            withContext(Dispatchers.IO) {
                ParayoApi.instance.signin(request)
            }

    private fun onSigninResponse(response: ApiResponse<SigninResponse>) {
        if(response.success && response.message == "user check success" && response.data != null) {
            Prefs.token = response.data.token
            Prefs.userName = response.data.userName
            Prefs.userId = response.data.userId
            onSigninSuccessToastShow()
        }
        else if(response.success && response.message == "user check failed") {
            onSigninFailedToastShow()
        }
        else if(response.success && response.message == "no user") {
            onNoUserToastShow()
        }
        else {
            onErrorToastShow()
        }
    }

    companion object {
        private const val TAG = "SigninViewModel"
    }
}