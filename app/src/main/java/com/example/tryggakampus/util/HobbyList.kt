package com.example.tryggakampus.util

import com.example.tryggakampus.R

object HobbyList {
    // Pair of hobby ID and string resource
    val allHobbies = listOf(
        "reading" to R.string.hobby_reading,
        "gaming" to R.string.hobby_gaming,
        "traveling" to R.string.hobby_traveling,
        "cooking" to R.string.hobby_cooking,
        "sports" to R.string.hobby_sports,
        "music" to R.string.hobby_music,
        "art" to R.string.hobby_art,
        "photography" to R.string.hobby_photography,
        "dancing" to R.string.hobby_dancing,
        "hiking" to R.string.hobby_hiking,
        "fishing" to R.string.hobby_fishing,
        "swimming" to R.string.hobby_swimming,
        "coding" to R.string.hobby_coding,
        "yoga" to R.string.hobby_yoga,
        "meditation" to R.string.hobby_meditation,
        "writing" to R.string.hobby_writing,
        "cycling" to R.string.hobby_cycling,
        "board_games" to R.string.hobby_board_games,
        "movies" to R.string.hobby_movies,
        "gardening" to R.string.hobby_gardening,
        "knitting" to R.string.hobby_knitting,
        "crafts" to R.string.hobby_crafts,
        "volunteering" to R.string.hobby_volunteering,
        "languages" to R.string.hobby_languages,
        "running" to R.string.hobby_running,
        "singing" to R.string.hobby_singing,
        "collecting" to R.string.hobby_collecting,
        "shopping" to R.string.hobby_shopping,
        "theater" to R.string.hobby_theater,
        "baking" to R.string.hobby_baking
    )

    // Helper function to get display name for a hobby ID
    fun getDisplayName(context: android.content.Context, hobbyId: String): String {
        return allHobbies.find { it.first == hobbyId }?.second?.let { context.getString(it) } ?: hobbyId
    }
}