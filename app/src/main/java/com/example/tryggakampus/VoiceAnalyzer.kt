package com.example.tryggakampus

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.tryggakampus.network.VoiceApi
import java.io.File
import java.io.FileInputStream
import java.net.URI
import java.util.Locale

object VoiceAnalyzer {

    private val okClient = OkHttpClient()

    // Retrofit instance with a dummy base URL (we use @Url on calls)
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://localhost/")
        .client(okClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val voiceApi = retrofit.create(VoiceApi::class.java)

    // BuildConfig fields
    private val PROXY_URL = BuildConfig.VOICE_PROXY_URL
    private val API_URL = BuildConfig.VOICE_API_URL
    private val API_KEY = BuildConfig.VOICE_API_KEY
    private val APP_API_KEY = BuildConfig.APP_API_KEY

    // Optional: File API base for upload (hardcoded default; can be moved to BuildConfig if desired)
    private const val FILE_UPLOAD_BASE = "https://generativelanguage.googleapis.com/upload/v1beta/files"

    private fun guessMimeType(file: File): String {
        val name = file.name.lowercase(Locale.US)
        return when {
            name.endsWith(".wav") -> "audio/wav"
            name.endsWith(".mp3") -> "audio/mpeg"
            name.endsWith(".m4a") || name.endsWith(".aac") -> "audio/mp4"
            name.endsWith(".3gp") || name.endsWith(".3gpp") -> "audio/3gpp"
            name.endsWith(".ogg") || name.endsWith(".oga") -> "audio/ogg"
            else -> "application/octet-stream"
        }
    }

    private fun ensureAnalyzePath(url: String): String {
        return if (url.endsWith("/analyze")) url else url.trimEnd('/') + "/analyze"
    }

    private fun proxyRootFromAnalyzeUrl(url: String): String {
        return try {
            val uri = URI(url)
            val scheme = uri.scheme ?: "http"
            val host = uri.host ?: ""
            val port = if (uri.port != -1) ":${uri.port}" else ""
            "$scheme://$host$port/"
        } catch (_: Exception) {
            url
        }
    }

    private fun emulatorAndLocalAlts(url: String): List<String> {
        return try {
            val uri = URI(url)
            val scheme = uri.scheme ?: "http"
            val host = uri.host ?: return listOf(url)
            val port = if (uri.port != -1) uri.port else 3000
            val path = uri.path.takeIf { it?.isNotBlank() == true } ?: "/analyze"

            val base = mutableListOf("$scheme://$host:$port$path")
            if (host == "10.0.2.2") base += "$scheme://127.0.0.1:$port$path"
            if (host == "127.0.0.1") base += "$scheme://10.0.2.2:$port$path"
            base.distinct()
        } catch (_: Exception) {
            listOf(url)
        }
    }

    private fun candidates(): List<String> {
        val configured = PROXY_URL.trim()
        val list = mutableListOf<String>()
        if (configured.isNotEmpty()) list += ensureAnalyzePath(configured)

        // Include optional alternates from BuildConfig (comma/semicolon/space separated)
        val alt = (try { BuildConfig.VOICE_ALT_URLS } catch (_: Throwable) { "" }).trim()
        if (alt.isNotEmpty()) {
            alt.split(',', ';', ' ', '\n', '\t')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .forEach { list += ensureAnalyzePath(it) }
        }

        // Add emulator/local alternates so one build can work on both with minimal tweaks
        list.addAll(emulatorAndLocalAlts(list.firstOrNull() ?: "http://10.0.2.2:3000/analyze"))
        return list.distinct()
    }

    suspend fun pingProxy(): String = withContext(Dispatchers.IO) {
        val results = mutableListOf<String>()
        for (c in candidates()) {
            val root = proxyRootFromAnalyzeUrl(c)
            val req = Request.Builder().url(root).get().build()
            val line = try {
                okClient.newCall(req).execute().use { resp ->
                    "${resp.code} ${resp.message} at $root"
                }
            } catch (e: Exception) {
                "fail: ${e.message} ($root)"
            }
            results += line
        }
        results.joinToString("; ")
    }

    // --- Direct Google API helpers ---

    private fun readAllBytes(file: File): ByteArray {
        FileInputStream(file).use { fis ->
            return fis.readBytes()
        }
    }

    private fun parseGenerativeResponseBody(body: String): String {
        return try {
            val j = JSONObject(body)
            // Newer schema: candidates[0].content.parts[].text
            if (j.has("candidates")) {
                val cArr = j.getJSONArray("candidates")
                if (cArr.length() > 0) {
                    val first = cArr.getJSONObject(0)
                    // Try nested content.parts
                    if (first.has("content")) {
                        val content = first.getJSONObject("content")
                        if (content.has("parts")) {
                            val parts = content.getJSONArray("parts")
                            for (i in 0 until parts.length()) {
                                val p = parts.getJSONObject(i)
                                if (p.has("text")) return p.getString("text")
                                if (p.has("inline_data")) {
                                    // ignore binary parts here
                                }
                            }
                        }
                        // Sometimes content itself is a string
                        if (content.has("text")) return content.getString("text")
                    }
                    // Older shortcuts
                    if (first.has("output")) return first.getString("output")
                    if (first.has("text")) return first.getString("text")
                }
            }
            // Other shapes
            if (j.has("output")) return j.getString("output")
            if (j.has("candidatesText")) return j.getString("candidatesText")
            // Fallback: first string value
            val keys = j.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val v = j.opt(k)
                if (v is String) return v
            }
            body
        } catch (e: Exception) {
            body
        }
    }

    private suspend fun httpPostJson(url: String, json: String): Pair<Int, String> = withContext(Dispatchers.IO) {
        val body: RequestBody = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val req = Request.Builder().url(url).post(body).build()
        okClient.newCall(req).execute().use { resp ->
            Pair(resp.code, resp.body?.string() ?: "")
        }
    }

    private suspend fun generateWithInlineBase64(file: File, mime: String, prompt: String, apiUrl: String, apiKey: String): String? {
        val b64 = Base64.encodeToString(readAllBytes(file), Base64.NO_WRAP)
        val partAudio = JSONObject().apply {
            put("inline_data", JSONObject().apply {
                put("mime_type", mime)
                put("data", b64)
            })
        }
        val partText = JSONObject().apply { put("text", prompt) }
        val content = JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray(listOf(partAudio, partText)))
        }
        val payload = JSONObject().apply {
            put("contents", JSONArray(listOf(content)))
            // You can tune generation params if desired:
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.3)
            })
        }
        val urlWithKey = if (apiUrl.contains("?")) "$apiUrl&key=$apiKey" else "$apiUrl?key=$apiKey"

        // Minimal retries for 429/5xx
        var lastBody = ""
        repeat(3) { attempt ->
            val (code, body) = httpPostJson(urlWithKey, payload.toString())
            lastBody = body
            if (code in 200..299) return parseGenerativeResponseBody(body)
            if (code == 429 || code in 500..599) delay(400L * (attempt + 1)) else return null
        }
        return parseGenerativeResponseBody(lastBody)
    }

    private suspend fun uploadFileThenGenerate(file: File, mime: String, prompt: String, apiUrl: String, apiKey: String): String? {
        // Upload raw bytes via File API (upload/v1beta/files) using X-Goog-Upload-Protocol: raw
        val uploadUrl = "$FILE_UPLOAD_BASE?key=$apiKey"
        val raw = readAllBytes(file)
        val uploadReq = Request.Builder()
            .url(uploadUrl)
            .addHeader("X-Goog-Upload-Protocol", "raw")
            .addHeader("Content-Type", mime)
            .post(raw.toRequestBody(mime.toMediaTypeOrNull()))
            .build()

        val fileName: String?
        val fileUri: String?
        okClient.newCall(uploadReq).execute().use { resp ->
            val body = resp.body?.string() ?: return null
            if (!resp.isSuccessful) return null
            // Expect a JSON file resource; pick the identifier/uri/name fields if present
            val j = try { JSONObject(body) } catch (_: Exception) { JSONObject() }
            fileName = j.optString("name", null) // e.g., "files/abc123"
            fileUri = j.optString("uri", null)  // sometimes present
        }

        // Build generateContent payload referencing the uploaded file
        val fileRef = JSONObject().apply {
            put("file_data", JSONObject().apply {
                put("mime_type", mime)
                // Prefer uri if present, else name; API accepts file URIs; some versions accept file name
                if (!fileUri.isNullOrBlank()) put("file_uri", fileUri)
                else if (!fileName.isNullOrBlank()) put("file_name", fileName)
            })
        }
        val partText = JSONObject().apply { put("text", prompt) }
        val content = JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray(listOf(fileRef, partText)))
        }
        val payload = JSONObject().apply {
            put("contents", JSONArray(listOf(content)))
            put("generationConfig", JSONObject().apply { put("temperature", 0.3) })
        }
        val urlWithKey = if (apiUrl.contains("?")) "$apiUrl&key=$apiKey" else "$apiUrl?key=$apiKey"

        // Minimal retries
        var lastBody = ""
        repeat(3) { attempt ->
            val (code, body) = httpPostJson(urlWithKey, payload.toString())
            lastBody = body
            if (code in 200..299) return parseGenerativeResponseBody(body)
            if (code == 429 || code in 500..599) delay(400L * (attempt + 1)) else return null
        }
        return parseGenerativeResponseBody(lastBody)
    }

    /**
     * Analyze the voice from an audio file.
     * Order of attempts (no server):
     * 1) If proxy configured, use proxy (original flow).
     * 2) Else try File API upload + generateContent with file reference.
     * 3) Else try inline Base64 audio generateContent.
     * 4) Fallback to local/mock analysis.
     */
    suspend fun analyzeVoice(file: File): String = withContext(Dispatchers.IO) {
        if (!file.exists()) throw IllegalArgumentException("File does not exist")

        // 1) Proxy path (unchanged)
        val proxyConfigured = PROXY_URL.trim().isNotEmpty()
        if (proxyConfigured) {
            val part = MultipartBody.Part.createFormData(
                name = "file",
                filename = file.name,
                body = file.asRequestBody(guessMimeType(file).toMediaTypeOrNull())
            )
            val appKeyHeader = if (APP_API_KEY.isNotBlank()) APP_API_KEY.trim() else null
            var lastErr: Throwable? = null
            for (url in candidates()) {
                try {
                    val resp = voiceApi.analyze(appKeyHeader, url, part)
                    if (!resp.isSuccessful) throw Exception("Server returned ${resp.code()}: ${resp.message()}")
                    val body = resp.body()
                    val text = body?.text
                    return@withContext text ?: "(empty response)"
                } catch (e: Throwable) {
                    lastErr = e; continue
                }
            }
            throw Exception("All endpoints failed: ${lastErr?.message}")
        }

        // 2/3) Direct Google API
        val apiUrl = API_URL.trim()
        val apiKey = API_KEY.trim()
        val mime = guessMimeType(file)
        val sizeKb = (file.length() / 1024).coerceAtLeast(1L)
        val prompt = "Analyze the provided audio. File: ${file.name}, size=${sizeKb}KB. " +
            "Identify whether the speaker sounds happy or stressed, give one short label and a brief explanation."

        if (apiUrl.isNotEmpty() && apiKey.isNotEmpty()) {
            // Try upload then generate with file reference
            try {
                uploadFileThenGenerate(file, mime, prompt, apiUrl, apiKey)?.let { return@withContext it }
            } catch (_: Exception) {}
            // Try inline Base64 single-call
            try {
                generateWithInlineBase64(file, mime, prompt, apiUrl, apiKey)?.let { return@withContext it }
            } catch (_: Exception) {}
        }

        // 4) Local/mock fallback to keep UI working
        try {
            val nameLower = file.name.lowercase(Locale.getDefault())
            val label = when {
                "angry" in nameLower || sizeKb > 512 -> "Stressed / High energy"
                "sad" in nameLower || sizeKb < 50 -> "Sad / Low energy"
                "happy" in nameLower -> "Happy / Positive"
                else -> "Neutral / Calm"
            }
            val explanation = "(Local mock) File ${file.name}, size=${sizeKb}KB. Placeholder analysis."
            return@withContext "$label — $explanation"
        } catch (e: Exception) {
            return@withContext "Analysis failed locally: ${e.message}"
        }
    }
}
