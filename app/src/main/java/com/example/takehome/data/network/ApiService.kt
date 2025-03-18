package com.example.takehome.data.network

import com.example.takehome.data.model.ItemModel
import retrofit2.http.GET

interface ApiService {
    @GET("hiring.json")
    suspend fun getItems(): List<ItemModel>
}