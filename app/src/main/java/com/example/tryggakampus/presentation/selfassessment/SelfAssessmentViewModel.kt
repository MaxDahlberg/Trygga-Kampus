package com.example.tryggakampus.presentation.selfassessment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tryggakampus.data.model.SelfAssessment
import com.example.tryggakampus.data.repository.SelfAssessmentRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

enum class RangeOption(val days: Long?) {
    D7(7), D30(30), D90(90), D365(365), ALL(null)
}

data class SelfAssessmentUiState(
    val uid: String = "",
    val selectedDate: LocalDate = LocalDate.now(ZoneId.systemDefault()),
    val range: RangeOption = RangeOption.D30,
    val rangeStart: LocalDate? = LocalDate.now(ZoneId.systemDefault()).minusDays(30),
    val rangeEnd: LocalDate? = LocalDate.now(ZoneId.systemDefault()),
    val entries: List<SelfAssessment> = emptyList(),
    val today: SelfAssessment? = null,
    val fromCache: Boolean = false,
    val hasPendingWrites: Boolean = false,
    val selfImage: Int? = null,
    val selfEsteem: Int? = null,
    val selfReliance: Int? = null,
    val note: String = "",
    val saving: Boolean = false,
    val savedMessage: String? = null,
    val error: String? = null
)

class SelfAssessmentViewModel(
    private val repo: SelfAssessmentRepository = SelfAssessmentRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(SelfAssessmentUiState())
    val state: StateFlow<SelfAssessmentUiState> = _state.asStateFlow()

    private var rangeJob: Job? = null
    private var docJob: Job? = null

    init {
        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        _state.value = _state.value.copy(uid = uid)
        subscribeRange()
        subscribeSelectedDate()
    }

    private fun subscribeRange() {
        rangeJob?.cancel()
        val s = _state.value
        rangeJob = viewModelScope.launch {
            repo.observeRange(s.uid, s.rangeStart, s.rangeEnd).collectLatest { list ->
                _state.value = _state.value.copy(entries = list)
            }
        }
    }

    private fun subscribeSelectedDate() {
        docJob?.cancel()
        val s = _state.value
        docJob = viewModelScope.launch {
            repo.observeDoc(s.uid, s.selectedDate).collectLatest { d ->
                _state.value = _state.value.copy(
                    today = d.doc,
                    fromCache = d.fromCache,
                    hasPendingWrites = d.hasPendingWrites,
                    selfImage = d.doc?.selfImage,
                    selfEsteem = d.doc?.selfEsteem,
                    selfReliance = d.doc?.selfReliance,
                    note = d.doc?.note ?: "",
                    savedMessage = if (d.hasPendingWrites) "Saved offline" else null
                )
            }
        }
    }

    fun setRange(option: RangeOption) {
        val end = LocalDate.now(ZoneId.systemDefault())
        val start = option.days?.let { end.minusDays(it) }
        _state.value = _state.value.copy(range = option, rangeStart = start, rangeEnd = end)
        subscribeRange()
    }

    fun setSelectedDate(date: LocalDate) {
        _state.value = _state.value.copy(selectedDate = date)
        subscribeSelectedDate()
    }

    fun setSelfImage(v: Int?) { _state.value = _state.value.copy(selfImage = v) }
    fun setSelfEsteem(v: Int?) { _state.value = _state.value.copy(selfEsteem = v) }
    fun setSelfReliance(v: Int?) { _state.value = _state.value.copy(selfReliance = v) }
    fun setNote(v: String) { _state.value = _state.value.copy(note = v.take(500)) }

    fun save() {
        val s = _state.value
        if (s.uid.isBlank()) {
            _state.value = s.copy(error = "Not authenticated")
            return
        }
        // Validate values 1..10 or null
        fun valid(x: Int?) = x == null || (x in 1..10)
        if (!valid(s.selfImage) || !valid(s.selfEsteem) || !valid(s.selfReliance)) {
            _state.value = s.copy(error = "Values must be 1..10 or empty")
            return
        }
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(saving = true, error = null)
                repo.upsert(
                    uid = s.uid,
                    date = s.selectedDate,
                    selfImage = s.selfImage,
                    selfEsteem = s.selfEsteem,
                    selfReliance = s.selfReliance,
                    note = s.note.ifBlank { null }
                )
                _state.value = _state.value.copy(saving = false, savedMessage = "Saved")
            } catch (e: Exception) {
                _state.value = _state.value.copy(saving = false, error = e.message)
            }
        }
    }

    fun deleteCurrent() {
        val s = _state.value
        if (s.uid.isBlank()) return
        viewModelScope.launch {
            try {
                repo.delete(s.uid, s.selectedDate)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun generateCsv(entries: List<SelfAssessment>): String {
        val sb = StringBuilder()
        sb.appendLine("date,selfImage,selfEsteem,selfReliance,hasNote")
        entries.forEach {
            val hasNote = !it.note.isNullOrBlank()
            sb.appendLine("${it.date},${it.selfImage ?: ""},${it.selfEsteem ?: ""},${it.selfReliance ?: ""},${if (hasNote) 1 else 0}")
        }
        return sb.toString()
    }
}
