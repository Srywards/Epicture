package fr.arouillard.epicture.api

import okhttp3.Interceptor
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Retrofit
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException

object AuthService {

    val api: Imgur.Auth
        get() {
            val client = OkHttpClient.Builder()
                    .addInterceptor(object : Interceptor {
                        @Throws(IOException::class)
                        override fun intercept(chain: Interceptor.Chain): Response {
                            val authed = chain.request()
                                    .newBuilder()
                                    .addHeader("Authorization", "Bearer " + Store.get("access_token"))
                                    .build()
                            return chain.proceed(authed)
                        }
                    }).build()

            return Retrofit.Builder()
                    .baseUrl(Imgur.IMGUR_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build()
                    .create(Imgur.Auth::class.java)
        }

    val unsplash: Unsplash.Auth
        get(){
            val client = OkHttpClient.Builder()
                    .addInterceptor (object : Interceptor {
                        @Throws(IOException::class)
                        override fun intercept(chain: Interceptor.Chain): Response {
                            val urls = chain.request()
                                    ?.url()
                                    ?.newBuilder()
                                    ?.addQueryParameter("client_id", Unsplash.ACCESS_KEY)
                                    ?.build()
                            val requests = chain.request()
                                    ?.newBuilder()
                                    ?.url(urls)
                                    ?.build()
                            return chain.proceed(requests)
                        }
                    }).build()

            return Retrofit.Builder()
                    .baseUrl(Unsplash.UNSPLASH_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build()
                    .create(Unsplash.Auth::class.java)
        }
}