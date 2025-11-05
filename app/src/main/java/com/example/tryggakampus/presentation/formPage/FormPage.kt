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
import androidx.compose.ui.res.stringResource
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
                Text(
                    stringResource(R.string.get_in_touch),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    stringResource(R.string.receive_help_text),
                    fontSize = 14.sp
                )

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
) {
    OutlinedInput(
        value = name,
        onValueChange = onChangeName,
        label = stringResource(R.string.enter_your_name),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedInput(
        value = subject,
        onValueChange = onChangeSubject,
        label = stringResource(R.string.subject_field),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedInput(
        value = message,
        onValueChange = onChangeMessage,
        label = stringResource(R.string.enter_your_message)
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
                Toast.makeText(
                    ctx,
                    ctx.getString(R.string.fill_out_all_fields),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val recipientEmail = "ibrx2002forwork@gmail.com"
                val senderName = if (name.isEmpty()) ctx.getString(R.string.stories_anonymous_label) else name
                val emailBody = ctx.getString(
                    R.string.email_body_template,
                    senderName,
                    message
                )

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
            stringResource(R.string.submit),
            fontSize = 20.sp
        )
    }
}