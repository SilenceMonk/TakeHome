package com.example.takehome.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.takehome.data.model.ItemModel
import com.example.takehome.data.viewmodel.ItemViewModel

import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ItemViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Add this line to collect the refreshing state
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }

    // The rest of the HomeScreen composable stays the same until the Success case
    Scaffold(
        topBar = {
            // TopAppBar code stays the same
            TopAppBar(
                title = {
                    AnimatedVisibility(
                        visible = !isSearchExpanded,
                        enter = fadeIn() + expandHorizontally(),
                        exit = fadeOut() + shrinkHorizontally()
                    ) {
                        Text("Fetch Rewards")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    // Expandable Search Bar
                    ExpandableSearchBar(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        isExpanded = isSearchExpanded,
                        onExpandedChange = { isSearchExpanded = it }
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Content based on state
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is ItemViewModel.UiState.Loading -> {
                        LoadingScreen()
                    }
                    is ItemViewModel.UiState.Empty -> {
                        EmptyScreen(onRetry = { viewModel.retry() })
                    }
                    is ItemViewModel.UiState.Success -> {
                        // Filter items based on search query
                        val filteredGroups = if (searchQuery.isBlank()) {
                            state.groupedItems
                        } else {
                            state.groupedItems.mapValues { (_, items) ->
                                items.filter { item ->
                                    item.name?.contains(searchQuery, ignoreCase = true) ?: false
                                }
                            }.filter { it.value.isNotEmpty() }
                        }

                        if (filteredGroups.isEmpty() && searchQuery.isNotEmpty()) {
                            // Show no results message when search has no matches
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No matching items found")
                            }
                        } else {
                            // Pass isRefreshing and onRefresh to ItemsList
                            ItemsList(
                                groupedItems = filteredGroups,
                                isRefreshing = isRefreshing,
                                onRefresh = { viewModel.retry() }
                            )
                        }
                    }
                    is ItemViewModel.UiState.Error -> {
                        ErrorScreen(message = state.message, onRetry = { viewModel.retry() })
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading items...")
        }
    }
}

@Composable
fun EmptyScreen(onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No items found")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Error: $message",
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
fun ItemsList(
    groupedItems: Map<Int, List<ItemModel>>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    // State to track which groups are expanded (all collapsed by default)
    val expandedState = remember { mutableStateMapOf<Int, Boolean>() }

    // Setup swipe refresh state
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = onRefresh
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Get sorted list of listIds
            val sortedListIds = groupedItems.keys.sorted()

            for (listId in sortedListIds) {
                item {
                    GroupHeader(
                        listId = listId,
                        isExpanded = expandedState.getOrDefault(listId, false),
                        itemCount = groupedItems[listId]?.size ?: 0,
                        onToggleExpand = {
                            expandedState[listId] = expandedState[listId] != true
                        }
                    )
                }

                // Only show items if the group is expanded
                if (expandedState.getOrDefault(listId, false)) {
                    items(groupedItems[listId] ?: emptyList()) { item ->
                        ItemCard(item = item)
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun GroupHeader(
    listId: Int,
    isExpanded: Boolean,
    itemCount: Int,
    onToggleExpand: () -> Unit
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "arrow_rotation"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "List ID: $listId",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "($itemCount items)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.rotate(arrowRotation)
        )
    }
    Divider(thickness = 2.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
}

@Composable
fun ItemCard(item: ItemModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = item.name ?: "",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "ID: ${item.id}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        // Search icon (collapsed state)
        if (!isExpanded) {
            IconButton(onClick = { onExpandedChange(true) }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Expanded search field
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandHorizontally(expandFrom = Alignment.End),
            exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.End)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search items...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (searchQuery.isNotEmpty()) {
                                onSearchQueryChange("")
                            } else {
                                onExpandedChange(false)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = if (searchQuery.isNotEmpty()) "Clear search" else "Close search"
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth().padding(start = 4.dp),
                colors = androidx.compose.material3.TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
            )
        }
    }
}