package com.example.tryggakampus.presentation.landingPage
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tryggakampus.LocalNavController
import com.example.tryggakampus.R
import com.example.tryggakampus.Routes

import com.example.tryggakampus.presentation.component.PageContainer


@Composable
fun LandingPage(title: String) {
    PageContainer(modifier = Modifier
        .background(MaterialTheme.colorScheme.background)
        .padding(15.dp)
    ) {
        Column (verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Logo()
            AboutUs()
            GetHelp()
        }
    }
}


@Composable
fun Logo() {
    Row (
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.logo_2),
            contentDescription = "Logo"
        )

        Text(
            text = stringResource(R.string.title),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 30.dp),
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun AboutUs() {
    Column (
        modifier = Modifier
            .clip(shape = RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Text(
            text = stringResource(R.string.about_us),
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = stringResource(R.string.info1),
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 14.sp
        )
    }
}

@Composable
fun GetHelp() {
    val navController = LocalNavController.current

    Column (
        modifier = Modifier
            .clip(shape = RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.secondary)
            .padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Text(
            text = stringResource(R.string.button),
            color = MaterialTheme.colorScheme.onSecondary,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = stringResource(R.string.buttonInfo),
            color = Color.Black,
            fontSize = 14.sp
        )

        Button(
            onClick = { navController.navigate(Routes.FormPage()) },
            modifier = Modifier.padding(top = 26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground
            )
        ) {
            Text(
                text = "Get in touch!",
                fontWeight = FontWeight.Bold
            )
        }
    }
}