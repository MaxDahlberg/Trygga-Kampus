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
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tryggakampus.R
import com.example.tryggakampus.presentation.component.PageContainer
import com.example.tryggakampus.util.AppThemeOption
import com.example.tryggakampus.util.LanguageManager
import com.example.tryggakampus.util.ThemeManager
import java.util.Locale

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
            Box(modifier = Modifier.testTag("settings_title")) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            // Language switch
            Box(modifier = Modifier.testTag("language_switch_section")) {
                SimpleLanguageSwitch(
                    currentLanguage = viewModel.currentLanguage,
                    onLanguageSelected = { languageCode ->
                        viewModel.setLanguage(languageCode)
                        LanguageManager.setAppLanguage(context, languageCode)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Theme switch
            Box(modifier = Modifier.testTag("theme_switch_section")) {
                SimpleThemeSwitch(
                    currentTheme = currentTheme,
                    onThemeSelected = { themeOption ->
                        ThemeManager.setTheme(themeOption, context)
                    }
                )
            }
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
            Box(modifier = Modifier.testTag("language_title_text")) {
                Text(
                    text = "App Language",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Box(modifier = Modifier.testTag("language_description_text")) {
                Text(
                    text = "Change the language of the app",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

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
                    Box(modifier = Modifier.testTag("current_language_text")) {
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
                }

                // Language selection buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // English
                    Box(modifier = Modifier.testTag("english_language_button")) {
                        Button(
                            onClick = { onLanguageSelected("en") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentLanguage == "en") MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text("EN")
                        }
                    }

                    // French
                    Box(modifier = Modifier.testTag("french_language_button")) {
                        Button(
                            onClick = { onLanguageSelected("fr") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentLanguage == "fr") MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text("FR")
                        }
                    }

                    // Swedish
                    Box(modifier = Modifier.testTag("swedish_language_button")) {
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
            }

            Box(modifier = Modifier.testTag("language_restart_notice_text")) {
                Text(
                    text = "The app will restart to apply changes",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Box(modifier = Modifier.testTag("language_note_text")) {
                Text(
                    text = "Note: Translations was provided by AI",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
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
            Box(modifier = Modifier.testTag("theme_title_text")) {
                Text(
                    text = stringResource(R.string.app_theme_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Box(modifier = Modifier.testTag("theme_description_text")) {
                Text(
                    text = stringResource(R.string.app_theme_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppThemeOption.entries.forEach { option ->
                        Box(modifier = Modifier.testTag("theme_${option.name.lowercase(Locale.ROOT)}_button")) {
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
            }

            Box(modifier = Modifier.testTag("theme_restart_notice_text")) {
                Text(
                    text = stringResource(R.string.app_theme_restart_notice),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}