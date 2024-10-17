package com.example.tryggakampus.presentation.surveyPage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tryggakampus.data.SurveyQuestions

@Composable
fun SurveyPage(title: String) {
    val questions = SurveyQuestions.questions
    var answers = remember { mutableStateListOf(*Array(questions.size) { "" }) }
    //still need val in order to store the questions

    // title area
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp))
    {
        item {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // questions and inputfield area
        itemsIndexed(questions) { index, question ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {

                    Text(
                        text = "Question ${index + 1}: $question",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )

                    OutlinedTextField(
                        value = answers[index],
                        onValueChange = { answers[index] = it },
                        label = { Text("Your Answer") },
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            //color of trygga klassen - light blue
                            focusedIndicatorColor = Color(0xFF68C3CD),
                        )
                    )
                }
            }
        }

        // button area after the questions
        item {
            Spacer(
                modifier = Modifier
                    .height(16.dp))
            Button(
                onClick = {
                    //button logic here for storing info in dB once its setup
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF68C3CD),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(4.dp)
            ) {
                Text(
                    "Submit Answers",
                    fontSize = 18.sp
                    )
            }
        }
    }
}
