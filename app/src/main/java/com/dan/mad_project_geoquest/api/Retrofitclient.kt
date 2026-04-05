package com.dan.mad_project_geoquest.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val API_KEY = "577p2m"
const val BASE_URL = "https://mark0s.com/geoquest/v1/"

object RetrofitClient {

    private val logger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val client by lazy {
        OkHttpClient.Builder()
            .addInterceptor(logger)
            .build()
    }

    val instance: GeoQuestApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder().create()
                )
            )
            .build()
            .create(GeoQuestApiService::class.java)
    }
}