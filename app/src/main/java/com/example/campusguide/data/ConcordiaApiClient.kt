package com.example.campusguide.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ConcordiaApiClient {
    private const val BASE_URL = "https://opendata.concordia.ca/"

    val service: ConcordiaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ConcordiaApiService::class.java)
    }
}
