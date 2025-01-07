package com.example.newble.Ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF27E021),
    secondary = Color(0xFF03DAC6),
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF93EE00),
    secondary = Color(0xFF03DAC6),
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

val CustomTypography = Typography(

    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Light,
        fontSize = 14.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Light,
        fontSize = 12.sp
    )
)

@Composable
fun NewbleTheme(
    primaryColor: Color = Color(0xFF93EE00),
    secondaryColor: Color = Color(0xFF03DAC6),
    backgroundColor: Color = Color.White,
    surfaceColor: Color = Color.White,
    onPrimaryColor: Color = Color.White,
    onSecondaryColor: Color = Color.Black,
    onBackgroundColor: Color = Color.Black,
    onSurfaceColor: Color = Color.Black,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    isCustom: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isCustom) {
        if (isDarkTheme) {
            darkColorScheme(
                primary = primaryColor,
                secondary = secondaryColor,
                background = backgroundColor,
                surface = surfaceColor,
                onPrimary = onPrimaryColor,
                onSecondary = onSecondaryColor,
                onBackground = onBackgroundColor,
                onSurface = onSurfaceColor
            )
        } else {
            lightColorScheme(
                primary = primaryColor,
                secondary = secondaryColor,
                background = backgroundColor,
                surface = surfaceColor,
                onPrimary = onPrimaryColor,
                onSecondary = onSecondaryColor,
                onBackground = onBackgroundColor,
                onSurface = onSurfaceColor
            )
        }
    }else{
            if (isDarkTheme) {
                darkColorScheme(
                    primary = DarkColorScheme.primary,
                    secondary = DarkColorScheme.secondary,
                    background = DarkColorScheme.background,
                    surface = DarkColorScheme.surface,
                    onPrimary = DarkColorScheme.onPrimary,
                    onSecondary = DarkColorScheme.onSecondary,
                    onBackground = DarkColorScheme.onBackground,
                    onSurface = DarkColorScheme.onSurface
                )
            } else {
                lightColorScheme(
                    primary = LightColorScheme.primary,
                    secondary = LightColorScheme.secondary,
                    background = LightColorScheme.background,
                    surface = LightColorScheme.surface,
                    onPrimary = LightColorScheme.onPrimary,
                    onSecondary = LightColorScheme.onSecondary,
                    onBackground = LightColorScheme.onBackground,
                    onSurface = LightColorScheme.onSurface
                )
            }
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CustomTypography,
        content = content
    )
}