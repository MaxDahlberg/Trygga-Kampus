package com.example.tryggakampus.network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url

data class AnalysisResponse(val text: String?)

interface VoiceApi {
    @Multipart
    @POST
    suspend fun analyze(
        @Header("X-APP-KEY") appKey: String?,
        @Url url: String,
        @Part file: MultipartBody.Part
    ): Response<AnalysisResponse>
}

