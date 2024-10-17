package com.example.tryggakampus.presentation.landingPage
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    val navController = LocalNavController.current

    val gradientColors = listOf(
        Color.Cyan,
        Color.hsl(hue = 210f, saturation = 0.6f, lightness = 0.8f),
        Color.hsl(hue = 330f, saturation = 0.7f, lightness = 0.7f))

    PageContainer(
        modifier = Modifier
            .background(Color.hsl(169f, 1f,0.93f))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row {
                Image(
                    painter = painterResource(R.drawable.logo_2),
                    contentDescription = "Logo"
                )
                Spacer(
                    modifier = Modifier
                        .padding(10.dp)
                )
                Text(
                    text = stringResource(R.string.title),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 30.dp),
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    fontSize = 30.sp,
                    style = TextStyle(
                        brush = Brush.linearGradient(
                            colors = gradientColors
                        )
                    ),
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Cursive
                )
            }

            Spacer(
                modifier = Modifier
                    .padding(50.dp)
            )

            Text(
                text = stringResource(R.string.about_us),
                modifier = Modifier,
                color = Color.Black,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.W700
            )
            Spacer(
                modifier = Modifier
                    .padding(9.dp)
            )
            Text(
                text = stringResource(R.string.info1),
                modifier = Modifier,
                color = Color.Black,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.W300
            )
            Spacer(
                modifier = Modifier
                    .padding(36.dp)
            )
            Text(
                text = stringResource(R.string.button),
                modifier = Modifier,
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.W700,
                textAlign = TextAlign.Left
            )
            Spacer(
                modifier = Modifier
                    .padding(9.dp)
            )
            Text(
                text = stringResource(R.string.buttonInfo),
                modifier = Modifier,
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.W300,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = {
                    navController.navigate(Routes.ForumPage())
                },
                modifier = Modifier
                    .padding(top = 26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                )
            ) {
                Text(
                    text = "Get in touch!",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
