// app/src/main/java/com/example/tryggakampus/data/SurveyQuestions.kt
package com.example.tryggakampus.data

import android.content.Context
import com.example.tryggakampus.R

object SurveyQuestions {
    fun getQuestions(context: Context): List<String> = listOf(
        context.getString(R.string.survey_question_1),
        context.getString(R.string.survey_question_2),
        context.getString(R.string.survey_question_3),
        context.getString(R.string.survey_question_4),
        context.getString(R.string.survey_question_5),
        context.getString(R.string.survey_question_6)
    )

}