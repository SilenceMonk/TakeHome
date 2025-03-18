package com.example.takehome.data.repository


import com.example.takehome.data.model.ItemModel
import com.example.takehome.data.network.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemRepository @Inject constructor(
    private val apiService: ApiService
) {
    fun getItems(): Flow<List<ItemModel>> = flow {
        val response = apiService.getItems()

        // Filter out items with null or blank names
        val filteredItems = response.filter { !it.name.isNullOrBlank() }

        // Sort by listId then by name
        val sortedItems = filteredItems.sortedWith(
            compareBy<ItemModel> { it.listId }
                .thenBy { it.name ?: "" }
        )
        emit(sortedItems)
    }
}