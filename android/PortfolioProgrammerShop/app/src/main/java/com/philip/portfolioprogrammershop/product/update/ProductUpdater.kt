package com.philip.portfolioprogrammershop.product.update

import android.util.Log
import com.philip.portfolioprogrammershop.api.ParayoApi
import com.philip.portfolioprogrammershop.api.request.ProductUpdateRequest
import com.philip.portfolioprogrammershop.api.response.ApiResponse
import com.philip.portfolioprogrammershop.api.response.ProductListItemResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductUpdater {

    suspend fun updater(request: ProductUpdateRequest) = when {
        request.isNotValidName ->
            ApiResponse.error("상품명을 조건에 맞게 입력해주세요.")
        request.isNotValidDescription ->
            ApiResponse.error("상품 설명을 조건에 맞게 입력해주세요.")
        request.isNotValidPrice ->
            ApiResponse.error("가격을 조건에 맞게 입력해주세요.")
        else -> withContext(Dispatchers.IO) {
            try {
                ParayoApi.instance.updateSingleProduct(
                    request.userId,
                    request.token,
                    request.product_id,
                    request.product_name,
                    request.product_description,
                    request.product_price,
                    request.imageMultipartBody,
                    request.imageRequestBody)

            } catch (e: Exception) {
                Log.e("ProductRegistrar_TAG", "상품 등록 오류. e:$e")
                ApiResponse.error<ProductListItemResponse>(
                    "알 수 없는 오류가 발생했습니다."
                )
            }
        }
    }

}