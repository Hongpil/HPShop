package com.philip.portfolioprogrammershop.product.registration

import android.util.Log
import com.philip.portfolioprogrammershop.api.ParayoApi
import com.philip.portfolioprogrammershop.api.request.ProductRegistrationRequest
import com.philip.portfolioprogrammershop.api.response.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class ProductRegistrar {

    suspend fun register(request: ProductRegistrationRequest) = when {
        request.isNotValidName ->
            ApiResponse.error("상품명을 조건에 맞게 입력해주세요.")
        request.isNotValidDescription ->
            ApiResponse.error("상품 설명을 조건에 맞게 입력해주세요.")
        request.isNotValidPrice ->
            ApiResponse.error("가격을 조건에 맞게 입력해주세요.")
        request.isNotValidCategoryId ->
            ApiResponse.error("카테고리 아이디를 선택해주세요.")
        else -> withContext(Dispatchers.IO) {
            try {
                ParayoApi.instance.registerProduct(
                        request.token,
                        request.userId,
                        request.name,
                        request.description,
                        request.price,
                        request.categoryId,
                        request.imageMultipartBody,
                        request.imageRequestBody)

            } catch (e: Exception) {
                Log.e("ProductRegistrar_TAG", "상품 등록 오류. e:$e")
                ApiResponse.error<Response<Void>>(
                    "알 수 없는 오류가 발생했습니다."
                )
            }
        }
    }

}