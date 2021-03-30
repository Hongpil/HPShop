package com.philip.portfolioprogrammershop.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class ApiGenerator {

    fun <T> generate(api: Class<T>): T = Retrofit.Builder()
            .baseUrl(URL_DEFAULT)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .client(httpClient())
            .build()
            .create(api)

    private fun httpClient() =
            OkHttpClient.Builder().apply {
                addInterceptor(httpLoggingInterceptor())
            }
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()

    private fun httpLoggingInterceptor() =
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

    companion object {
        const val URL_DEFAULT = ""

    }

}