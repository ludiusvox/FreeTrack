plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp") version "2.0.21-1.0.27"
}

android {
    namespace = "com.freeapp.freetrack"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.freeapp.freetrack"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // 100% Free Core AndroidX & Material 3
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    // 1. Native FusedLocationProviderClient (Google Play Services Location - 100% Free)
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // 2. Local Database: Room (Offline SQLite - Zero Cloud / $0/mo forever)
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // 3. UI Layouts: MPAndroidChart (Elevation Profiles & Weight Trends)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // 4. UI Layouts: OsmDroid (100% Free OpenStreetMap - No Google Maps API Key or billing needed!)
    implementation("org.osmdroid:osmdroid-android:6.1.18")

    // Coroutines for asynchronous sensor and DB I/O
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // Preference for OsmDroid
    implementation("androidx.preference:preference-ktx:1.2.1")
}