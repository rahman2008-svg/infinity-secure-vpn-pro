package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PremiumDarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    secondary = SecondaryDark,
    tertiary = PremiumOrange,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = OnPrimaryDark,
    onSecondary = OnSecondaryDark,
    onBackground = OnBackgroundDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = Color(0xFF1E2E4A),
    onSurfaceVariant = Color(0xFFCFD8DC),
    error = Color(0xFFFF5252)
)

// Since this is a premium VPN app, we force a sleek and cohesive Dark Theme Experience by default
// which is exactly what VPN/Proxy apps like 1.1.1.1 do for a futuristic security atmosphere.
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme for the premium cyber security feel
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PremiumDarkColorScheme,
        typography = Typography,
        content = content
    )
}
