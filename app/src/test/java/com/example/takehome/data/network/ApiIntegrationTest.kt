package com.example.takehome.data.network

import com.example.takehome.data.model.ItemModel
import com.example.takehome.data.repository.ItemRepository
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService
    private lateinit var repository: ItemRepository

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val client = OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.SECONDS)
            .readTimeout(1, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS)
            .build()

        val gson = GsonBuilder().create()

        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)

        repository = ItemRepository(apiService)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `repository processes API response correctly`() = runBlocking {
        // Prepare mock JSON response
        val mockResponse = """
            [
                {"id": 1, "listId": 1, "name": "B Item"},
                {"id": 2, "listId": 1, "name": "A Item"},
                {"id": 3, "listId": 2, "name": "C Item"},
                {"id": 4, "listId": 2, "name": null},
                {"id": 5, "listId": 3, "name": ""},
                {"id": 6, "listId": 3, "name": "E Item"},
                {"id": 7, "listId": 1, "name": "C Item"}
            ]
        """.trimIndent()

        // Enqueue mock response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockResponse)
        )

        // Get items through repository
        val items = repository.getItems().first()

        // Verify filtering - should have 5 items (2 with null/blank names are filtered out)
        assertEquals(5, items.size)

        // Verify all items have non-null and non-blank names
        assertTrue(items.all { !it.name.isNullOrBlank() })

        // Verify sorting by listId then name
        val expectedOrder = listOf(
            ItemModel(id = 2, listId = 1, name = "A Item"),
            ItemModel(id = 1, listId = 1, name = "B Item"),
            ItemModel(id = 7, listId = 1, name = "C Item"),
            ItemModel(id = 3, listId = 2, name = "C Item"),
            ItemModel(id = 6, listId = 3, name = "E Item")
        ) // Removed .filterIndexed

        // Check all fields including id
        assertEquals(expectedOrder.size, items.size)
        expectedOrder.forEachIndexed { index, expectedItem ->
            assertEquals(expectedItem.id, items[index].id)
            assertEquals(expectedItem.listId, items[index].listId)
            assertEquals(expectedItem.name, items[index].name)
        }
    }

    @Test
    fun `repository handles error response correctly`() = runBlocking {
        // Enqueue error response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody("{}")
        )

        try {
            repository.getItems().first()
            fail("Expected an exception but none was thrown")
        } catch (e: Exception) {
            // Test passes if an exception is thrown
            assertTrue(true)
        }
    }
}