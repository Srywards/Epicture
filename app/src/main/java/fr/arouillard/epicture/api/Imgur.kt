package fr.arouillard.epicture.api

import android.support.annotation.Nullable
import java.util.ArrayList

import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

import fr.arouillard.epicture.model.Imgur.Basic
import fr.arouillard.epicture.model.Imgur.Image
import fr.arouillard.epicture.model.Imgur.Avatar

interface Imgur {


    interface Auth {

        @GET("3/account/{username}/images")
        fun images(@Path("username") username: String): Call<Basic<ArrayList<Image>>>

        @GET("3/gallery/hot/{mode}/?album_previews=true")
        fun trends(@Path("mode") mode: String): Call<Basic<ArrayList<Image>>>

        @GET("3/account/{username}/favorites/{page}/newest")
        fun favorites(@Path("username") username: String, @Path("page") page: Int): Call<Basic<ArrayList<Image>>>

        @GET("3/gallery/search/{mode}/all/{page}")
        fun search(@Path("mode") mode: String, @Path("page") page: Int, @Query("q") keyword: String): Call<Basic<ArrayList<Image>>>

        @GET("3/account/{username}/avatar")
        fun avatar(@Path("username") username: String): Call<Basic<Avatar>>

        @Multipart
        @POST("3/upload")
        fun uploadImage(@Part("image") image: RequestBody): Call<Basic<Image>>

        @POST("3/image/{id}/favorite")
        fun favorite(@Path("id") id: String): Call<Basic<String>>

    }

    companion object {
        const val IMGUR_BASE_URL = "https://api.imgur.com"
        const val IMGUR_CLIENT_ID = "5046b154c081c83"
        const val AUTHORIZATION_URL = ("https://api.imgur.com/oauth2/authorize?client_id=" + IMGUR_CLIENT_ID
                + "&response_type=token")
        const val REDIRECT_URI = "https://epicture:8080"
    }

}