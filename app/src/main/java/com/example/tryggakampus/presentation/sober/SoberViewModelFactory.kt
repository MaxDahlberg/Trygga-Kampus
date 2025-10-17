package com.example.tryggakampus.presentation.sober

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tryggakampus.data.soberActivity.SoberActivityRepository
import com.example.tryggakampus.dataStore

class SoberViewModelFactory(private val appContext: Context): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(SoberViewModel::class.java))
        return SoberViewModel(SoberActivityRepository(appContext.dataStore)) as T
    }
}
