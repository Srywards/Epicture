package fr.arouillard.epicture.api

import java.util.ArrayList

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

import fr.arouillard.epicture.model.Unsplash.Basic
import fr.arouillard.epicture.model.Unsplash.Image


interface Unsplash {


    interface Auth {

        @GET("photos")
        fun photos(): Call<ArrayList<Image>>

        @GET("search/photos")
        fun search(@Query("page") page: Int = 0, @Query("query") query: String, @Query("per_page") per_page: Int = 20): Call<Basic<ArrayList<Image>>>

    }

    companion object {
        const val UNSPLASH_BASE_URL = "https://api.unsplash.com/"
        const val ACCESS_KEY = "439477d1fb6a70bc9836c5bd2cad958ef99e7be015c45f7d291dcfec84d07d6c"
    }

}