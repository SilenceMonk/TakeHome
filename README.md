# Fetch Rewards Android Coding Challenge

This Android application fetches and displays a list of items from the Fetch Rewards API, meeting specific sorting and filtering requirements.

## Features

- Retrieves data from the Fetch Rewards API endpoint
- Filters out items with null or blank names
- Groups items by their "listId"
- Sorts items first by "listId" then by "name"
- Displays the organized data in an easy-to-read list
- Handles loading states, errors, and empty results with appropriate UI feedback
- Provides retry functionality for error recovery

## Architecture


This application follows the MVVM (Model-View-ViewModel) architecture pattern with Clean Architecture principles:

- **UI Layer**: Jetpack Compose UI components
- **Presentation Layer**: ViewModel manages UI state
- **Domain Layer**: Repository handles business logic
- **Data Layer**: Remote data source via Retrofit

### Key Components

- **ItemViewModel**: Manages UI state and business logic
- **ItemRepository**: Handles data operations and transformations
- **ApiService**: Defines network endpoints
- **NetworkModule**: Provides network dependencies via Hilt
- **Composables**: Modular UI components built with Jetpack Compose

## Technologies Used

- **Kotlin** - Primary programming language
- **Jetpack Compose** - Modern UI toolkit for native UI
- **Coroutines & Flow** - Asynchronous programming
- **Hilt 2.48** - Dependency injection
- **Retrofit 2.9.0** - Type-safe HTTP client
- **OkHttp 4.11.0** - HTTP client for logging and intercepting requests
- **Material 3** - Design system for modern Android UI
- **ViewModel & StateFlow** - State management for UI
- **Lifecycle-Runtime-Compose 2.6.2** - Lifecycle-aware Compose utilities

## Project Structure

```
com.example.fetchrewards/
├── FetchRewardsApplication.kt  # Application class with Hilt
├── MainActivity.kt             # Entry point activity
├── data/
│   ├── model/
│   │   └── ItemModel.kt        # Data model for items
│   ├── network/
│   │   ├── ApiService.kt       # Retrofit service interface
│   │   └── RetrofitClient.kt   # Network configuration
│   ├── repository/
│   │   └── ItemRepository.kt   # Data operations and business logic
│   └── viewmodel/
│       └── ItemViewModel.kt    # UI state management
└── ui/
    ├── screens/
    │   └── HomeScreen.kt       # Main screen with list display
    └── theme/                  # Material theming components
```

## Setup Instructions

### Prerequisites

- Android Studio Arctic Fox (2020.3.1) or newer
- Kotlin 1.6.0 or newer
- Android SDK 31 (Android 12) or newer
- JDK 11

### Building the App

1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Run the app on an emulator or physical device (minimum API 33)

## Design Decisions & Trade-offs

### Data Processing Approach

The app processes API data in the following order:
1. Fetch raw data from the API
2. Filter out null/blank named items
3. Sort by listId then by name (using natural number sorting for names)
4. Group by listId for display

### UI Design

- Used Material 3 design components for a modern look and feel
- Implemented cards for each item for visual separation
- Group headers clearly indicate the listId grouping
- Loading, error, and empty states provide feedback to the user

### Performance Considerations

- Data processing occurs in the repository layer, offloaded from the UI thread
- StateFlow is used for efficient UI updates
- Network timeouts are configured for reliability

## Future Improvements

- Add unit tests for Repository and ViewModel layers
- Add UI tests for Compose components
- Implement local caching for offline support
- Add pull-to-refresh functionality
- Enhance error handling with more specific error messages
- Implement search functionality
- Add animations for smoother UI transitions