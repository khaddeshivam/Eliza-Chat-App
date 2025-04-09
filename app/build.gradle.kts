plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.eliza.messenger"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.eliza.messenger"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "DEBUG_PROVIDER_TOKEN", "\"8E5B7C7C-DE18-4E3D-8B7B-B4EDAE8D9FA4\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }
}

dependencies {
    // AndroidX Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Glide
    implementation (libs.github.glide)

    // Google Play Services
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    // Material Design
    implementation("com.google.android.material:material:1.11.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.12.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-appcheck")
    implementation("com.google.firebase:firebase-appcheck-playintegrity")
    debugImplementation("com.google.firebase:firebase-appcheck-debug")

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth:22.3.0")
    
    // Firebase App Check
    implementation("com.google.firebase:firebase-appcheck:17.1.0")
    implementation("com.google.firebase:firebase-appcheck-playintegrity:17.1.0")

    // Architecture Components
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime:2.7.0")

    // Navigation Components
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")

    // Image Loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.firebase.crashlytics.buildtools)
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // RxJava
    implementation("io.reactivex.rxjava3:rxjava:3.1.8")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")

    // Networking
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // WebRTC
    implementation (libs.android)

    // DiffUtil for RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
