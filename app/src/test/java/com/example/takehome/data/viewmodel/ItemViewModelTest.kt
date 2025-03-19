package com.example.takehome.data.viewmodel

import com.example.takehome.data.model.ItemModel
import com.example.takehome.data.repository.ItemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@ExperimentalCoroutinesApi
class ItemViewModelTest {

    private lateinit var repository: ItemRepository
    private lateinit var viewModel: ItemViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock(ItemRepository::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `items are grouped by listId when successful`() = runTest {
        // Given a sorted list of items with different listIds
        val mockItems = listOf(
            ItemModel(id = 1, listId = 1, name = "A Item"),
            ItemModel(id = 2, listId = 1, name = "B Item"),
            ItemModel(id = 3, listId = 2, name = "A Item"),
            ItemModel(id = 4, listId = 2, name = "B Item"),
            ItemModel(id = 5, listId = 3, name = "A Item")
        )

        // Mock repository to return our test data
        `when`(repository.getItems()).thenReturn(flow { emit(mockItems) })

        // Initialize ViewModel with mocked repository
        viewModel = ItemViewModel(repository)

        // Allow the coroutines to complete
        advanceUntilIdle()

        // Get the current UI state
        val currentState = viewModel.uiState.value

        // Verify state is Success
        assertTrue(currentState is ItemViewModel.UiState.Success)

        // Cast to Success and verify grouping
        val successState = currentState as ItemViewModel.UiState.Success
        val groupedItems = successState.groupedItems

        // Verify there are 3 groups (listId 1, 2, and 3)
        assertEquals(3, groupedItems.size)

        // Verify each group has the correct number of items
        assertEquals(2, groupedItems[1]?.size)
        assertEquals(2, groupedItems[2]?.size)
        assertEquals(1, groupedItems[3]?.size)

        // Verify items in each group have the correct listId
        groupedItems.forEach { (listId, items) ->
            assertTrue(items.all { it.listId == listId })
        }
    }

    @Test
    fun `empty result sets state to Empty`() = runTest {
        // Mock repository to return empty list
        `when`(repository.getItems()).thenReturn(flow { emit(emptyList()) })

        // Initialize ViewModel with mocked repository
        viewModel = ItemViewModel(repository)

        // Allow the coroutines to complete
        advanceUntilIdle()

        // Get the current UI state
        val currentState = viewModel.uiState.value

        // Verify state is Empty
        assertTrue(currentState is ItemViewModel.UiState.Empty)
    }

    @Test
    fun `error in repository sets state to Error`() = runTest {
        // Mock repository to throw an exception
        val errorMessage = "Network error"
        `when`(repository.getItems()).thenReturn(flow { throw Exception(errorMessage) })

        // Initialize ViewModel with mocked repository
        viewModel = ItemViewModel(repository)

        // Allow the coroutines to complete
        advanceUntilIdle()

        // Get the current UI state
        val currentState = viewModel.uiState.value

        // Verify state is Error with the correct message
        assertTrue(currentState is ItemViewModel.UiState.Error)
        assertEquals(errorMessage, (currentState as ItemViewModel.UiState.Error).message)
    }
}