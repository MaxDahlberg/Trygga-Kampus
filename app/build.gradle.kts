import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    kotlin("plugin.serialization") version "2.0.21"
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.tryggakampus"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.tryggakampus"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Added from remote branch
        resConfigs("en", "fr", "sv", "fi", "ja")

        // BuildConfig fields for voice API configuration; can be provided via:
        // 1) Gradle project properties (e.g., -PVOICE_PROXY_URL=...)
        // 2) app/voice.properties (not committed) with keys VOICE_PROXY_URL, VOICE_API_URL, VOICE_API_KEY, APP_API_KEY
        val voiceProps = Properties().apply {
            val f = file("voice.properties")
            if (f.exists()) f.inputStream().use { this.load(it) }
        }

        fun prop(key: String): String {
            val fromProject = project.findProperty(key) as? String
            if (!fromProject.isNullOrBlank()) return fromProject
            return voiceProps.getProperty(key) ?: ""
        }

        val voiceApiUrl: String = prop("VOICE_API_URL")
        val voiceApiKey: String = prop("VOICE_API_KEY")
        val voiceProxyUrl: String = prop("VOICE_PROXY_URL")
        val appApiKey: String = prop("APP_API_KEY")
        val voiceAltUrls: String = prop("VOICE_ALT_URLS")
        val appCheckDebugToken: String = prop("APP_CHECK_DEBUG_TOKEN")
        val rtdbUrl: String = prop("RTDB_URL")

        buildConfigField("String", "VOICE_API_URL", "\"$voiceApiUrl\"")
        buildConfigField("String", "VOICE_API_KEY", "\"$voiceApiKey\"")
        buildConfigField("String", "VOICE_PROXY_URL", "\"$voiceProxyUrl\"")
        buildConfigField("String", "APP_API_KEY", "\"$appApiKey\"")
        buildConfigField("String", "VOICE_ALT_URLS", "\"$voiceAltUrls\"")
        buildConfigField("String", "APP_CHECK_DEBUG_TOKEN", "\"$appCheckDebugToken\"")
        buildConfigField("String", "RTDB_URL", "\"$rtdbUrl\"")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
        // Enable generation of BuildConfig so custom buildConfigField entries are available at runtime
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Firebase
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-firestore")

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)

    // Navigation
    implementation(libs.navigation.compose)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.dynamic)
    implementation(libs.navigation.ui)
    implementation(libs.androidx.navigation.runtime.ktx)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-extended") // Keep hardcoded to avoid alias issues

    // Habit tracker
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0") // ViewModel Compose

    // Serialization
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.datastore.preferences.core)

    // Third-party
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.database)
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.facebook.android:facebook-login:16.3.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    //Coil
    implementation("io.coil-kt:coil-compose:2.5.0")
    //FireStore & Storage
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-firestore")
    implementation(libs.firebase.firestore.ktx)

    // Firebase App Check (Debug provider for local/dev)
    implementation("com.google.firebase:firebase-appcheck")
    implementation("com.google.firebase:firebase-appcheck-debug")

    // Compose foundation for LazyVerticalGrid
    implementation("androidx.compose.foundation:foundation")

    // Media3 ExoPlayer for video playback and UI PlayerView
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")

    // Added for VoiceNote feature: OkHttp + Coroutines
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.google.firebase:firebase-analytics")
}
