package com.example.tryggakampus.presentation.settingsPage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

@OptIn(ExperimentalLayoutApi::class)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    // UPDATED to use stringResource for translation
                    text = stringResource(R.string.settings_language_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = when (currentLanguage) {
                        "fr" -> "🇫🇷 Français"
                        "sv" -> "🇸🇪 Svenska"
                        "fi" -> "🇫🇮 Suomi"
                        "ja" -> "🇯🇵 日本語"
                        else -> "🇬🇧 English"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }


            Text(
                // UPDATED to use stringResource for translation
                text = stringResource(R.string.settings_language_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val languages = mapOf(
                    "en" to "🇬🇧",
                    "fr" to "🇫🇷",
                    "sv" to "🇸🇪",
                    "fi" to "🇫🇮",
                    "ja" to "🇯🇵"
                )
                languages.forEach { (langCode, flag) ->
                    Button(
                        onClick = { onLanguageSelected(langCode) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentLanguage == langCode) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(flag)
                    }
                }
            }

            Text(
                // UPDATED to use stringResource for translation
                text = stringResource(R.string.settings_language_restart_notice),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                // UPDATED to use stringResource for translation
                text = stringResource(R.string.settings_language_ai_note),
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