package com.omargannoune.smartbudget.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    secondary = SecondaryAccent,
    tertiary = PrimaryAccent,
    background = Background,
    surface = Surface,
    surfaceVariant = SurfaceElevated,
    onPrimary = Background,
    onSecondary = Background,
    onTertiary = Background,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = Error,
    onError = Background,
    outline = Divider
)

@Composable
fun SmartBudgetTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
