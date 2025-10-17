package com.example.tryggakampus.data.soberActivity

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

data class SoberActivityState(
    val streakDays: Int = 0,
    val lastCheckin: LocalDate? = null
)

class SoberActivityRepository(private val dataStore: DataStore<Preferences>) {
    private object Keys {
        val STREAK = intPreferencesKey("soberactivity_streak")
        val LAST = stringPreferencesKey("soberactivity_last_checkin")
    }

    val state: Flow<SoberActivityState> = dataStore.data.map { p ->
        val streak = p[Keys.STREAK] ?: 0
        val last = p[Keys.LAST]?.let(LocalDate::parse)
        SoberActivityState(streak, last)
    }

    suspend fun checkIn(today: LocalDate = LocalDate.now()) {
        dataStore.updateData { prefs ->
            val last = prefs[Keys.LAST]?.let(LocalDate::parse)
            val newStreak = when {
                last == null -> 1
                last == today -> prefs[Keys.STREAK] ?: 0
                last == today.minusDays(1) -> (prefs[Keys.STREAK] ?: 0) + 1
                else -> 1
            }
            prefs.toMutablePreferences().apply {
                this[Keys.STREAK] = newStreak
                this[Keys.LAST] = today.toString()
            }
        }
    }

    suspend fun reset() {
        dataStore.updateData { prefs ->
            prefs.toMutablePreferences().apply {
                this[Keys.STREAK] = 0
                remove(Keys.LAST)
            }
        }
    }
}
