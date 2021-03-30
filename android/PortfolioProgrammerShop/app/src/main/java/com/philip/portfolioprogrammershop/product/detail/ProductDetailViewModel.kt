package com.philip.portfolioprogrammershop.product.detail

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.philip.portfolioprogrammershop.App
import com.philip.portfolioprogrammershop.api.ApiGenerator.Companion.URL_DEFAULT
import com.philip.portfolioprogrammershop.api.ParayoApi
import com.philip.portfolioprogrammershop.api.response.ApiResponse
import com.philip.portfolioprogrammershop.api.response.ProductResponse
import com.philip.portfolioprogrammershop.common.Prefs
import com.philip.portfolioprogrammershop.util.Event
import kotlinx.coroutines.runBlocking
import java.text.NumberFormat


class ProductDetailViewModel : ViewModel() {

    val productName = MutableLiveData("-")
    val description = MutableLiveData("")
    val price = MutableLiveData("-")


    // "상품 정보"
    private val _productInfo = MutableLiveData<ProductInfo>()
    val productInfo:LiveData<ProductInfo> = _productInfo
    private fun setProductInfo (info: ProductInfo) {
        _productInfo.postValue(info)
    }

    // "상품 이미지"
    private val _productImage = MutableLiveData<String>()
    val productImage: LiveData<String> = _productImage
    private fun onProductImage (imagePath: String) {
        _productImage.postValue(imagePath)
    }

    // "내 상품 유무"
    private val _checkMe = MutableLiveData<Event<Boolean>>()
    val checkMe: LiveData<Event<Boolean>> = _checkMe
    private fun onCheckMe() {
        _checkMe.postValue(Event(true))
    }

    // "상품 삭제 결과"
    private val _deleteProductToast = MutableLiveData<Event<Boolean>>()
    val deleteProductToast: LiveData<Event<Boolean>> = _deleteProductToast
    private fun onDeleteProductSuccess() {
        _deleteProductToast.postValue(Event(true))
    }


    /**
     * 상품 조회
     */
    fun loadDetail(productId:Long) {
        val response = getProduct(productId)
        onGetProductResponse(response)
    }
    private fun getProduct(productId:Long) = runBlocking {
        try {
            ParayoApi.instance.getSingleProduct(Prefs.userId, Prefs.token, productId)
        } catch (e: Exception) {
            ApiResponse.error<ProductResponse>(
                    "상품 정보를 가져오는 중 오류가 발생했습니다."
            )
        }
    }
    private fun onGetProductResponse(response: ApiResponse<ProductResponse>) {
        if (response.success && response.message == "product get success") {
            response.data?.let {
                // 내가 올린 상품인 경우 => 상품 수정,삭제 버튼 보일 것
                if (Prefs.userId == it.user_id) onCheckMe()
                val commaSeparatedPrice = NumberFormat.getInstance().format(it.price)
                productName.value = it.name.replace("\"", "")  // 쌍따옴표 제거
                description.value = it.description.replace("\"", "")  // 쌍따옴표 제거
                price.value = "₩${commaSeparatedPrice}"
                onProductImage("${URL_DEFAULT}/${it.path}")

                /**
                 * 수정, 삭제 기능을 위해 상품정보를 LiveData 에 저장해둔다.
                 */
                setProductInfo(ProductInfo(it.id, it.name, it.description, it.price, it.path))
            }
        } else {
            Toast.makeText(App.instance, response.message ?: "알 수 없는 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 상품 삭제
     */
    fun deleteItem(productId: Long) {
        val response = deleteProduct(productId)
        onDeleteProductResponse(response)
    }
    private fun deleteProduct(productId: Long) = runBlocking {
        try {
            ParayoApi.instance.deleteSingleProduct(Prefs.userId, Prefs.token, productId)
        } catch (e: Exception) {
            ApiResponse.error<Void>(
                "상품 정보를 삭제하는 중 오류가 발생했습니다."
            )
        }
    }
    private fun onDeleteProductResponse(response: ApiResponse<Void>) {
        if (response.success && response.message == "product delete success") {
            onDeleteProductSuccess()
        } else {
            Toast.makeText(App.instance, response.message ?: "알 수 없는 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val TAG = "ProductDetailViewModel"
    }

}