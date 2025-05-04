package com.bambiloff.kvantor.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary        = Color(0xFF8C52FF),  // кнопки і іконки
    onPrimary      = Color.White,
    secondary      = Color(0xFF8C52FF),
    onSecondary    = Color.White,
    tertiary       = Color(0xFF8C52FF),
    onTertiary     = Color.White,
    background     = Color(0xFF390D58),  // фон екрану
    onBackground   = Color.White,
    surface        = Color(0xFF390D58),
    onSurface      = Color.White,
    error          = Color(0xFFCF6679),
    onError        = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary        = Purple40,
    onPrimary      = Color.White,
    secondary      = PurpleGrey40,
    onSecondary    = Color.White,
    tertiary       = Pink40,
    onTertiary     = Color.White,
    background     = Color.White,
    onBackground   = Color.Black,
    surface        = Color.White,
    onSurface      = Color.Black,
    error          = Color(0xFFB00020),
    onError        = Color.White
)

@Composable
fun KvantorTheme(
    darkTheme: Boolean = true,       // тепер за замовчуванням — темна тема
    dynamicColor: Boolean = false,   // Monet вимкнено
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
