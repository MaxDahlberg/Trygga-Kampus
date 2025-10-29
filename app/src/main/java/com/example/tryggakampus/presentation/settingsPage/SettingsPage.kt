package com.example.tryggakampus.presentation.settingsPage

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tryggakampus.R
import com.example.tryggakampus.presentation.component.PageContainer
import com.example.tryggakampus.util.AppThemeOption
import com.example.tryggakampus.util.LanguageManager
import com.example.tryggakampus.util.ThemeManager

@Composable
fun SettingsPage(
    viewModel: SettingsPageViewModel = viewModel(),
    title: String
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val currentTheme by ThemeManager.currentTheme.collectAsState() // Observe theme

    LaunchedEffect(configuration) {
        val currentLanguage = configuration.locales[0].language
        viewModel.setLanguage(currentLanguage)
    }

    PageContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Language switch
            SimpleLanguageSwitch(
                currentLanguage = viewModel.currentLanguage,
                onLanguageSelected = { languageCode ->
                    viewModel.setLanguage(languageCode)
                    LanguageManager.setAppLanguage(context, languageCode)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Theme switch
            SimpleThemeSwitch(
                currentTheme = currentTheme,
                onThemeSelected = { themeOption ->
                    ThemeManager.setTheme(themeOption, context)
                }
            )
        }
    }
}

@Composable
fun SimpleLanguageSwitch(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "App Language",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Change the language of the app",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Current language display
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = when (currentLanguage) {
                            "fr" -> "Français"
                            "sv" -> "Svenska"
                            else -> "English"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Language selection buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // English
                    Button(
                        onClick = { onLanguageSelected("en") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentLanguage == "en") MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text("EN")
                    }

                    // French
                    Button(
                        onClick = { onLanguageSelected("fr") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentLanguage == "fr") MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text("FR")
                    }

                    // Swedish
                    Button(
                        onClick = { onLanguageSelected("sv") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentLanguage == "sv") MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text("SV")
                    }
                }
            }

            Text(
                text = "The app will restart to apply changes",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Note: Translations was provided by AI",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}


@Composable
fun SimpleThemeSwitch(
    currentTheme: AppThemeOption,
    onThemeSelected: (AppThemeOption) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.app_theme_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = stringResource(R.string.app_theme_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppThemeOption.entries.forEach { option ->
                        Button(
                            onClick = { onThemeSelected(option) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentTheme == option)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = when (option) {
                                    AppThemeOption.LIGHT -> stringResource(R.string.theme_light)
                                    AppThemeOption.DARK -> stringResource(R.string.theme_dark)
                                    AppThemeOption.SYSTEM -> stringResource(R.string.theme_system)
                                }
                            )
                        }
                    }
                }
            }

            Text(
                text = stringResource(R.string.app_theme_restart_notice),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}