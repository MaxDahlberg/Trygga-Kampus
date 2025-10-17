package com.example.tryggakampus.presentation.sober

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tryggakampus.data.soberActivity.SoberActivityRepository
import com.example.tryggakampus.data.soberActivity.SoberActivityState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class SoberViewModel(private val repo: SoberActivityRepository): ViewModel() {
    val state: StateFlow<SoberActivityState> = repo.state.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), SoberActivityState()
    )
    fun checkIn(today: LocalDate = LocalDate.now()) = viewModelScope.launch { repo.checkIn(today) }
    fun reset() = viewModelScope.launch { repo.reset() }
}
