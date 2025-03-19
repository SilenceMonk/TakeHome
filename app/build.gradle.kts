plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.fetchrewards"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.fetchrewards"
        minSdk = 33
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Keep existing dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Add networking libraries
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    // Add lifecycle components
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Add Hilt for dependency injection
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // ===== TESTING DEPENDENCIES =====

    // JUnit and core testing
    testImplementation(libs.junit)
    testImplementation(libs.androidx.core.testing)

    // Mockito for mocking in tests
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)


    // Coroutines testing
    testImplementation(libs.kotlinx.coroutines.test)

    // MockWebServer for API testing
    testImplementation(libs.mockwebserver)

    // === Android Testing ===
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.core)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.rules)
    androidTestImplementation(libs.androidx.espresso.core)

    // === Compose Testing ===
    // Use platform BOM for Compose UI testing
    androidTestImplementation(platform(libs.androidx.compose.bom))
    // Testing dependencies
    androidTestImplementation(libs.ui.test.junit4)
    // Debug implementation for createComposeRule
    debugImplementation(libs.ui.test.manifest)
    // For visual debugging during testing
    debugImplementation(libs.ui.tooling)

}

// Add kapt configuration for Hilt
kapt {
    correctErrorTypes = true
}