package com.example.takehome.data.viewmodel

import com.example.takehome.data.repository.ItemRepository
import com.example.takehome.data.model.ItemModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItemViewModel @Inject constructor(
    private val repository: ItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    init {
        loadItems()
    }

    private fun loadItems() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.getItems()
                .catch { e ->
                    _uiState.value = UiState.Error(e.message ?: "Unknown error occurred")
                }
                .collect { items ->
                    if (items.isEmpty()) {
                        _uiState.value = UiState.Empty
                    } else {
                        // Group items by listId
                        val groupedItems = items.groupBy { it.listId }
                        _uiState.value = UiState.Success(groupedItems)
                    }
                }
        }
    }

    fun retry() {
        loadItems()
    }

    sealed class UiState {
        data object Loading : UiState()
        data object Empty : UiState()
        data class Success(val groupedItems: Map<Int, List<ItemModel>>) : UiState()
        data class Error(val message: String) : UiState()
    }
}