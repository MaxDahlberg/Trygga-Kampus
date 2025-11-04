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
        resConfigs("en", "fr", "sv")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    testOptions {
        unitTests.all {
            it.jvmArgs("-noverify")
        }
    }



    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    packaging {
        resources {
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
            excludes += "/META-INF/AL2.0"
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
    implementation("androidx.compose.material:material-icons-extended")

    // Habit tracker
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // Serialization
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.datastore.preferences.core)

    // Third-party
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
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    // testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("io.mockk:mockk:1.13.5")
    androidTestImplementation("io.mockk:mockk-android:1.13.8")
    testImplementation("io.mockk:mockk-inline:1.13.5")
    testRuntimeOnly("io.mockk:mockk-agent-jvm:1.13.5")
    testImplementation("app.cash.turbine:turbine:1.1.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("org.jacoco:org.jacoco.core:0.8.13")
    androidTestImplementation(libs.androidx.espresso.core)

    // Android instrumented testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.4")
    androidTestImplementation("androidx.compose.ui:ui-test-manifest:1.5.4")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.4")
    debugImplementation(libs.androidx.ui.tooling)
}
