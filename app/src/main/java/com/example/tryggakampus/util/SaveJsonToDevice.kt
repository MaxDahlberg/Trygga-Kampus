package com.example.tryggakampus.util

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File

fun saveJsonToDownloads(context: Context, json: String, fileName: String) {
    try {
        // Handle old Android versions (6–9) with runtime permission
        if (Build.VERSION.SDK_INT in Build.VERSION_CODES.M..Build.VERSION_CODES.P) {
            val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                // Request permission and exit early
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(permission),
                    1001
                )
                Toast.makeText(context, "Please grant storage permission and try again.", Toast.LENGTH_LONG).show()
                return
            }
        }

        // Android 10+ (Q and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val uri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues
            ) ?: throw Exception("Failed to create file in Downloads")

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toByteArray())
                outputStream.flush()
            }

        } else {
            // Android 6–9
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()

            val file = File(downloadsDir, fileName)
            file.writeText(json)

            // Make file visible in file manager
            MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null, null)
        }

        Toast.makeText(context, "File saved to Downloads", Toast.LENGTH_SHORT).show()

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to save file: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
