package com.example.takehome.data.model

import com.google.gson.annotations.SerializedName

data class ItemModel(
    @SerializedName("id") val id: Int,
    @SerializedName("listId") val listId: Int,
    @SerializedName("name") val name: String?
)