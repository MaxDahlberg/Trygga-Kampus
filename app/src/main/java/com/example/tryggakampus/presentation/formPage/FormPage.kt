package com.example.tryggakampus.presentation.formPage

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tryggakampus.R
import com.example.tryggakampus.presentation.component.OutlinedInput
import com.example.tryggakampus.presentation.component.PageContainer

@Composable
fun FormPage(title: String) {

    val name = remember { mutableStateOf(TextFieldValue()) }
    val subject = remember { mutableStateOf(TextFieldValue()) }
    val message = remember { mutableStateOf(TextFieldValue()) }

    PageContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.helping_hands1),
                contentDescription = "helping hands logo"
            )
            Column (modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary)
                .padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Get in touch", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                Text("receive the help you need", fontSize = 14.sp) // not sure, Per might want a different text here
                FormsFields(
                    name.value.text,
                    onChangeName = { name.value = TextFieldValue(it) },
                    subject.value.text,
                    onChangeSubject = { subject.value = TextFieldValue(it) },
                    message.value.text,
                    onChangeMessage = { message.value = TextFieldValue(it) }
                )
                SubmitButton(
                    name.value.text,
                    subject.value.text,
                    message.value.text
                )
            }
        }
    }
}

@Composable
fun FormsFields(
    name: String,
    onChangeName: (s: String) -> Unit,

    subject: String,
    onChangeSubject: (s: String) -> Unit,

    message: String,
    onChangeMessage: (s: String) -> Unit
){
    OutlinedInput(
        value = name,
        onValueChange = onChangeName,
        label = "Enter your name",
        singleLine = true
    )

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedInput(
        value = subject,
        onValueChange = onChangeSubject,
        label = "Subject Field",
        singleLine = true
    )

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedInput(
        value = message,
        onValueChange = onChangeMessage,
        label = "Enter your message here"
    )

    Spacer(modifier = Modifier.height(20.dp))
}

@Composable
fun SubmitButton(
    name: String,
    subject: String,
    message: String
) {
    val ctx = LocalContext.current

    Button(
        onClick = {
            if (subject.isEmpty() || message.isEmpty()) {
                // Show a toast if required fields are missing
                Toast.makeText(ctx, "Please fill out all fields except for Name.", Toast.LENGTH_SHORT).show()
            } else {
                val recipientEmail = "ibrx2002forwork@gmail.com"
                val senderName = if (name.isEmpty()) "Anonymous" else name
                val emailBody = "Hello, my name is $senderName.\n\n$message\n\nThank you!"
                val intent = Intent(Intent.ACTION_SEND)

                intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
                intent.putExtra(Intent.EXTRA_SUBJECT, subject)
                intent.putExtra(Intent.EXTRA_TEXT, emailBody)
                intent.type = "message/rfc822"

                ctx.startActivity(Intent.createChooser(intent, "Choose an Email client : "))
            }
        },
        modifier = Modifier
            .clip(shape = CircleShape)
            .size(width = 200.dp, height = 60.dp)
            .background(Color.Red),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Text(
            "Submit",
            fontSize = 20.sp
        )
    }
}