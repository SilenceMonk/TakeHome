package com.example.takehome.data.repository

import com.example.takehome.data.model.ItemModel
import com.example.takehome.data.network.ApiService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ItemRepositoryTest {

    private lateinit var apiService: ApiService
    private lateinit var repository: ItemRepository

    @Before
    fun setup() {
        apiService = mock(ApiService::class.java)
        repository = ItemRepository(apiService)
    }

    @Test
    fun `items with null or blank names are filtered out`() = runBlocking {
        // Given a list of items with some null or blank names
        val mockItems = listOf(
            ItemModel(id = 1, listId = 1, name = "Item 1"),
            ItemModel(id = 2, listId = 1, name = null),
            ItemModel(id = 3, listId = 2, name = ""),
            ItemModel(id = 4, listId = 2, name = "Item 4"),
            ItemModel(id = 5, listId = 3, name = "  "),
            ItemModel(id = 6, listId = 3, name = "Item 6")
        )

        `when`(apiService.getItems()).thenReturn(mockItems)

        // When fetching items
        val result = repository.getItems().first()

        // Then all items with null or blank names should be filtered out
        assertEquals(3, result.size)
        assertTrue(result.all { !it.name.isNullOrBlank() })
    }

    @Test
    fun `items are sorted first by listId then by name`() = runBlocking {
        // Given an unsorted list of items
        val mockItems = listOf(
            ItemModel(id = 3, listId = 2, name = "Z Item"),
            ItemModel(id = 5, listId = 3, name = "A Item"),
            ItemModel(id = 1, listId = 1, name = "B Item"),
            ItemModel(id = 2, listId = 1, name = "A Item"),
            ItemModel(id = 6, listId = 3, name = "C Item"),
            ItemModel(id = 4, listId = 2, name = "A Item")
        )

        `when`(apiService.getItems()).thenReturn(mockItems)

        // When fetching items
        val result = repository.getItems().first()

        // Then items should be sorted first by listId then by name
        assertEquals(6, result.size)

        // Check sorting by listId
        for (i in 0 until result.size - 1) {
            if (result[i].listId == result[i + 1].listId) {
                // If same listId, then compare names
                assertTrue("Items with same listId should be sorted by name",
                    result[i].name!! <= result[i + 1].name!!)
            } else {
                // Compare listIds
                assertTrue("Items should be sorted by listId",
                    result[i].listId < result[i + 1].listId)
            }
        }
    }
}