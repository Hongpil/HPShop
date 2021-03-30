package com.philip.portfolioprogrammershop.api

import com.philip.portfolioprogrammershop.api.request.SigninRequest
import com.philip.portfolioprogrammershop.api.response.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ParayoApi {

    @GET(ALL_PRODUCTS_GET)
    suspend fun getProducts(
            @Header("user_id") userId: Long?,
            @Header("user_token") userToken: String?,
            @Query("category_id") categoryId: Int?,
            @Query("keyword") keyword: String? = null
    ): ApiResponse<List<ProductListItemResponse>>

    @GET(SINGLE_PRODUCT_GET)
    suspend fun getSingleProduct(
            @Header("user_id") userId: Long?,
            @Header("user_token") userToken: String?,
            @Query("product_id") productId: Long?
    ): ApiResponse<ProductResponse>

    @POST(LOGIN_DATA)
    suspend fun signin(@Body signinRequest: SigninRequest)
            : ApiResponse<SigninResponse>

    @FormUrlEncoded
    @POST(SIGNUP_EMAIL_AUTH_REQUEST)
    suspend fun EmailAuth_Request(
            @Header("apiKey") token: String?,
            @Field("user_email") email: String?
    ): ApiResponse<Void>

    @FormUrlEncoded
    @POST(SIGNUP_USER_JOIN)
    suspend fun UserJoin_Request(
            @Header("apiKey") token: String?,
            @Field("email") userEmail: String?,
            @Field("name") userName: String?,
            @Field("password") userPassword: String?
    ): ApiResponse<String>

    @Multipart
    @POST(PRODUCT_REGISTRATION)
    suspend fun registerProduct(
            @Header("user_token") userToken: String?,
            @Part("user_id") userId: Long?,
            @Part("product_name") productName: String?,
            @Part("product_description") productDescription: String?,
            @Part("product_price") productPrice: Int?,
            @Part("product_category_id") productCategoryId: Int?,
            @Part image: MultipartBody.Part?,
            @Part("upload") name: RequestBody?
    ): ApiResponse<Response<Void>>

    @Multipart
    @POST(PRODUCT_UPDATE)
    suspend fun updateSingleProduct(
            @Header("user_id") userId: Long?,
            @Header("user_token") userToken: String?,
            @Part("product_id") productId: Long?,
            @Part("product_name") productName: String?,
            @Part("product_description") productDescription: String?,
            @Part("product_price") productPrice: Int?,
            @Part image: MultipartBody.Part?,
            @Part("upload") name: RequestBody?
    ): ApiResponse<ProductListItemResponse>

    @FormUrlEncoded
    @POST(PRODUCT_DELETE)
    suspend fun deleteSingleProduct(
            @Header("user_id") userId: Long?,
            @Header("user_token") userToken: String?,
            @Field("product_id") productId: Long?
    ): ApiResponse<Void>

    companion object {
        val instance = ApiGenerator()
            .generate(ParayoApi::class.java)

        const val LOGIN_DATA = "/LoginUserCheck"
        const val SIGNUP_EMAIL_AUTH_REQUEST = "/EmailAuth"
        const val SIGNUP_USER_JOIN = "/UserJoin"
        const val PRODUCT_REGISTRATION = "/ProductRegistration"
        const val ALL_PRODUCTS_GET = "/AllProductsGet"
        const val SINGLE_PRODUCT_GET = "/SingleProductGet"
        const val PRODUCT_UPDATE = "/ProductUpdate"
        const val PRODUCT_DELETE = "/ProductDelete"
    }

}