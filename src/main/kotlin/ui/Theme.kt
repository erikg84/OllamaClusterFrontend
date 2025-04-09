package ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object MatrixThemeColors {
    val background = Color(0xFF000000) // Pure black
    val surface = Color(0xFF0D0D0D) // Nearly black
    val cardBackground = Color(0xFF121212) // Slightly lighter than surface for cards
    val userMessageBg = Color(0xFF003B00) // Dark green
    val assistantMessageBg = Color(0xFF0F2318) // Darker green
    val userMessageText = Color(0xFF00FF00) // Bright matrix green
    val assistantMessageText = Color(0xFF4DFF4D) // Lighter matrix green
    val inputFieldBg = Color(0xFF0A1A0A) // Very dark green
    val buttonColor = Color(0xFF008F11) // Matrix code green
    val highlightColor = Color(0xFF00FF41) // Matrix highlight green
    val accentColor = Color(0xFF003B00) // Dark green accent
    val statusText = Color(0xFF4DFF4D) // Light green for status text
    val chartLine = Color(0xFF00FF41) // Bright green for charts
    val sectionHeader = Color(0xFF00FF41) // Bright green for section headers
    val separatorLine = Color(0xFF003B00) // Dark green for separators
    val errorColor = Color(0xFFFF5252) // Red for errors
    val infoColor = Color(0xFF4DFF4D) // Green for info
    val warningColor = Color(0xFFFFD740) // Yellow for warnings
    val debugColor = Color(0xFF40C4FF) // Blue for debug
}

// Light theme colors
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2B4162),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8E2FF),
    onPrimaryContainer = Color(0xFF001A41),
    secondary = Color(0xFF1E88E5),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1E4FF),
    onSecondaryContainer = Color(0xFF001D35),
    tertiary = Color(0xFF4CAF50),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB9F6CA),
    onTertiaryContainer = Color(0xFF002111),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF7F9FC),
    onBackground = Color(0xFF001F2A),
    surface = Color.White,
    onSurface = Color(0xFF001F2A),
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44474F),
    outline = Color(0xFF74777F)
)

// Dark theme colors
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF2B4162),
    onPrimary = Color(0xFFD8E2FF),
    primaryContainer = Color(0xFF003588),
    onPrimaryContainer = Color(0xFFD8E2FF),
    secondary = Color(0xFF1565C0),
    onSecondary = Color(0xFFD1E4FF),
    secondaryContainer = Color(0xFF004A86),
    onSecondaryContainer = Color(0xFFD1E4FF),
    tertiary = Color(0xFF388E3C),
    onTertiary = Color(0xFFB9F6CA),
    tertiaryContainer = Color(0xFF005321),
    onTertiaryContainer = Color(0xFFB9F6CA),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF001F2A),
    onBackground = Color(0xFFA6EEFF),
    surface = Color(0xFF001F25),
    onSurface = Color(0xFFA6EEFF),
    surfaceVariant = Color(0xFF44474F),
    onSurfaceVariant = Color(0xFFC5C6D0),
    outline = Color(0xFF8F9099)
)

// Create a composable function to provide the typography
@Composable
fun AppTypography(): Typography {
    return Typography(
        // Headers
        headlineLarge = MaterialTheme.typography.headlineLarge,
        headlineMedium = MaterialTheme.typography.headlineMedium,
        headlineSmall = MaterialTheme.typography.headlineSmall,

        // Titles
        titleLarge = MaterialTheme.typography.titleLarge,
        titleMedium = MaterialTheme.typography.titleMedium,
        titleSmall = MaterialTheme.typography.titleSmall,

        // Body text
        bodyLarge = MaterialTheme.typography.bodyLarge,
        bodyMedium = MaterialTheme.typography.bodyMedium,
        bodySmall = MaterialTheme.typography.bodySmall,

        // Labels
        labelLarge = MaterialTheme.typography.labelLarge,
        labelMedium = MaterialTheme.typography.labelMedium,
        labelSmall = MaterialTheme.typography.labelSmall
    )
}

@Composable
fun LLMClusterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography(),
        content = content
    )
}
