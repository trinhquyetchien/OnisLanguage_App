package com.onislanguage.app.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object OnisApiClient {
    const val DEFAULT_BASE_URL = "http://192.168.1.7:8000/"
    private var tokenProvider: (() -> String?)? = null

    fun setTokenProvider(provider: () -> String?) {
        tokenProvider = provider
    }

    fun resolveUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null
        if (path.startsWith("http://") || path.startsWith("https://")) return path
        return DEFAULT_BASE_URL + path.trimStart('/')
    }

    fun create(baseUrl: String = DEFAULT_BASE_URL): OnisApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(600, TimeUnit.SECONDS)
            .writeTimeout(600, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .addInterceptor(AuthInterceptor { tokenProvider?.invoke() })
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OnisApiService::class.java)
    }
}
