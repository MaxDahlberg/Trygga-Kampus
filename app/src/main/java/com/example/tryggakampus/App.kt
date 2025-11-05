package com.example.tryggakampus

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // If App Check is enforced on Storage/Firestore, install Debug provider for debug builds
        try {
            val token = try { BuildConfig.APP_CHECK_DEBUG_TOKEN } catch (_: Throwable) { "" }
            if (!token.isNullOrBlank()) {
                // Allow setting explicit debug token at runtime for convenience
                System.setProperty("firebase_app_check_debug_token", token)
                Log.d("App", "App Check debug token set from BuildConfig")
            }
            val appCheck = FirebaseAppCheck.getInstance()
            appCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
        } catch (_: Throwable) {}

        // Enable Firestore offline persistence explicitly (enabled by default on Android, but we set to be explicit)
        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings
    }
}
