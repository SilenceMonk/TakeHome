# Fetch Rewards Takehome App

A native Android application that retrieves, sorts, and displays data from Fetch Rewards API endpoint according to specified requirements.

## Demo

![App Demo](demo.gif)

## Features

- **Data Retrieval**: Fetches JSON data from the provided endpoint
- **Data Processing**: Filters and sorts data according to requirements
- **Grouping**: Groups items by listId with expandable/collapsible sections
- **Search(*)**: Allows filtering items by name
- **Pull-to-Refresh(*)**: Updates data with a standard pull gesture
- **Error Handling**: Provides clear feedback for loading, empty states, and errors

    (*: added for enhanced user experience)

## Requirements Implementation

The application fulfills all the requirements specified in the exercise:

1. **Retrieve data from the provided endpoint**
   - Uses Retrofit to fetch data from `https://fetch-hiring.s3.amazonaws.com/hiring.json`
   - Implemented in `ApiService.kt` and configured in `NetworkModule.kt`

2. **Filter out items with blank or null names**
   - Implemented in `ItemRepository.kt` using:
   ```kotlin
   val filteredItems = response.filter { !it.name.isNullOrBlank() }
   ```

3. **Sort by "listId" then by "name"**
   - Implemented in `ItemRepository.kt` using:
   ```kotlin
   val sortedItems = filteredItems.sortedWith(
       compareBy<ItemModel> { it.listId }
           .thenBy { it.name ?: "" }
   )
   ```

4. **Group items by "listId"**
   - Implemented in `ItemViewModel.kt`:
   ```kotlin
   val groupedItems = items.groupBy { it.listId }
   ```
   - UI representation in `HomeScreen.kt` shows collapsible group headers

5. **Display in an easy-to-read list**
   - Uses Material 3 design components
   - Implemented expandable sections with animation
   - Clean card-based design for individual items
   - Added search functionality for improved usability

## Architecture

The application follows MVVM (Model-View-ViewModel) architecture with a clean separation of concerns:

- **Data Layer**: Models, API service, and repository
- **ViewModel**: Manages UI state and business logic
- **UI Layer**: Composable functions for rendering the interface

### Key Components

- **Jetpack Compose**: For modern, declarative UI
- **Hilt**: For dependency injection
- **Coroutines & Flow**: For asynchronous operations
- **Material 3**: For consistent design language
- **ViewModel**: For managing UI state and surviving configuration changes
- **Repository Pattern**: For abstracting data sources

## Testing

The project includes comprehensive tests:

- **Unit Tests**: For repository and ViewModel logic
- **Integration Tests**: For API and data processing
- **Mock Testing**: Using Mockito for dependencies

Tests verify that:
- Items with null or blank names are filtered out
- Sorting is correctly applied (by listId then by name)
- Items are properly grouped by listId
- Error handling works as expected

## Running the Project

1. Clone the repository
2. Open in Android Studio (latest version)
3. Build and run on an emulator or physical device running the current Android release

## Dependencies

- Android Jetpack (Compose, ViewModel, etc.)
- Retrofit for network calls
- Hilt for dependency injection
- Kotlin Coroutines and Flow
- Material 3 Components