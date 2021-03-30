package com.philip.portfolioprogrammershop.product.list

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.philip.portfolioprogrammershop.App
import com.philip.portfolioprogrammershop.api.ParayoApi
import com.philip.portfolioprogrammershop.api.response.ApiResponse
import com.philip.portfolioprogrammershop.api.response.ProductListItemResponse
import com.philip.portfolioprogrammershop.common.Prefs
import kotlinx.coroutines.runBlocking

class ProductListViewModel : ViewModel() {

    // "상품 이미지"
    private val _allProducts = MutableLiveData<List<ProductListItemResponse>>()
    val allProducts: LiveData<List<ProductListItemResponse>> = _allProducts
    private fun saveAllProducts (products: List<ProductListItemResponse>) {
        _allProducts.postValue(products)
    }

    fun setDataSource(categoryId:Int, keyword:String?) {
        val response = getProducts(categoryId, keyword)
        onGetProductResponse(response)
    }

    private fun getProducts(categoryId:Int, keyword: String?) = runBlocking {
        try {
            ParayoApi.instance.getProducts(Prefs.userId, Prefs.token, categoryId, keyword)
        } catch (e: Exception) {
            ApiResponse.error<List<ProductListItemResponse>>(
                "알 수 없는 오류가 발생했습니다."
            )
        }
    }

    private fun onGetProductResponse(response: ApiResponse<List<ProductListItemResponse>>) {
        if (response.success && response.message == "products get success") {
            response.data?.let { saveAllProducts(it) }
        } else {
            Toast.makeText(App.instance, response.message ?: "알 수 없는 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val TAG = "ProductListViewModel"
    }
}