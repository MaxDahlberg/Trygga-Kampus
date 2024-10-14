package com.example.tryggakampus.domain.network_handling

import android.net.http.NetworkException
import android.os.Build
import androidx.annotation.RequiresExtension
import coil.network.HttpException
import com.example.tryggakampus.domain.model.NetworkErr
import com.example.tryggakampus.domain.model.NetworkError

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
fun Throwable.toNetworkError(): NetworkError {
    val error = when(this){
        is NetworkException -> NetworkErr.NoConnection
        is HttpException -> NetworkErr.UnknownResponse
        else -> NetworkErr.UnknownError
    }
    return NetworkError(
        error = error,
        throwable = this
    )
}