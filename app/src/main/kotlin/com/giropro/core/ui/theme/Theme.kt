package com.giropro.core.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * GiroPro usa tema escuro como identidade principal (motorista trabalha muito à noite),
 * com o amarelo como cor de destaque. Mantemos também um esquema claro coerente.
 */
private val DarkColors = darkColorScheme(
    primary = GiroAmarelo,
    onPrimary = GiroPreto,
    secondary = GiroVerde,
    onSecondary = GiroPreto,
    error = GiroVermelho,
    onError = GiroBranco,
    background = GiroPreto,
    onBackground = GiroBranco,
    surface = GiroCinza,
    onSurface = GiroBranco,
    surfaceVariant = GiroCinzaClaro,
    onSurfaceVariant = GiroTextoSecundario,
)

private val LightColors = lightColorScheme(
    primary = GiroAmarelo,
    onPrimary = GiroPreto,
    secondary = GiroVerde,
    onSecondary = GiroBranco,
    error = GiroVermelho,
    onError = GiroBranco,
    background = GiroBranco,
    onBackground = GiroPreto,
    surface = GiroBranco,
    onSurface = GiroPreto,
    surfaceVariant = Color(0xFFEDEDED),
    onSurfaceVariant = GiroCinza,
)

private val GiroTypography = Typography(
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black, fontSize = 34.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
)

@Composable
fun GiroProTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = GiroTypography,
        content = content,
    )
}
