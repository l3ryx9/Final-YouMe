package com.youme24.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────
//  Material 3 ColorScheme — "Pineapple Paradise 3D"
//  dynamicColor = false  →  palette de marque imposée
// ─────────────────────────────────────────────────────────────

private val DarkColorScheme = darkColorScheme(
    primary               = PineappleYellow,
    onPrimary             = Color(0xFF1A1200),
    primaryContainer      = OrangeDark,
    onPrimaryContainer    = OrangeLight,
    secondary             = TropicalGreenLight,
    onSecondary           = Color(0xFFFFFFFF),
    secondaryContainer    = TropicalGreenDark,
    onSecondaryContainer  = Color(0xFFA8F0A8),
    tertiary              = HibiscusCoral,
    onTertiary            = Color(0xFF1A0000),
    tertiaryContainer     = HibiscusDark,
    onTertiaryContainer   = HibiscusLight,
    error                 = ErrorCoral,
    onError               = Color(0xFF1A0000),
    background            = NightTropical,
    onBackground          = TextOnDark,
    surface               = SurfaceDark,
    onSurface             = TextOnDark,
    surfaceVariant        = CardDark,
    onSurfaceVariant      = TextSecondaryDark,
    outline               = OutlineDark,
    inverseSurface        = CoconutCream,
    inverseOnSurface      = TextOnLight,
    inversePrimary        = OrangeAnanas,
    scrim                 = Color(0xCC000000),
)

private val LightColorScheme = lightColorScheme(
    primary               = OrangeAnanas,
    onPrimary             = Color(0xFFFFFFFF),
    primaryContainer      = PineappleYellowLight,
    onPrimaryContainer    = OrangeDark,
    secondary             = TropicalGreen,
    onSecondary           = Color(0xFFFFFFFF),
    secondaryContainer    = Color(0xFFC8E6C9),
    onSecondaryContainer  = TropicalGreenDark,
    tertiary              = HibiscusCoral,
    onTertiary            = Color(0xFFFFFFFF),
    tertiaryContainer     = HibiscusLight,
    onTertiaryContainer   = HibiscusDark,
    error                 = ErrorCoral,
    onError               = Color(0xFFFFFFFF),
    background            = CoconutCream,
    onBackground          = TextOnLight,
    surface               = SurfaceLight,
    onSurface             = TextOnLight,
    surfaceVariant        = CardLight,
    onSurfaceVariant      = TextSecondaryLight,
    outline               = OutlineLight,
    inverseSurface        = NightTropical,
    inverseOnSurface      = TextOnDark,
    inversePrimary        = PineappleYellow,
    scrim                 = Color(0x80000000),
)

// ─────────────────────────────────────────────────────────────
//  Custom extras (bubble colors, status indicators)
//  accessible via LocalYouMeColors.current
// ─────────────────────────────────────────────────────────────

data class YouMeColors(
    val bubbleOwn: Color,
    val bubbleOwnGradientEnd: Color,
    val bubbleOther: Color,
    val onlineIndicator: Color,
    val readIndicator: Color,
    val errorIndicator: Color,
    val shadowBubble: Color,
    val reflectHighlight: Color,
    val geminiAccent: Color,
    val isDark: Boolean,
)

private val DarkYouMeColors = YouMeColors(
    bubbleOwn             = BubbleOwn,
    bubbleOwnGradientEnd  = BubbleOwnGradientEnd,
    bubbleOther           = BubbleOtherDark,
    onlineIndicator       = OnlineGreen,
    readIndicator         = ReadOrange,
    errorIndicator        = ErrorCoral,
    shadowBubble          = ShadowBubble,
    reflectHighlight      = ReflectWhite,
    geminiAccent          = GeminiViolet,
    isDark                = true,
)

private val LightYouMeColors = YouMeColors(
    bubbleOwn             = PineappleYellow,
    bubbleOwnGradientEnd  = OrangeAnanas,
    bubbleOther           = BubbleOtherLight,
    onlineIndicator       = OnlineGreen,
    readIndicator         = ReadOrange,
    errorIndicator        = ErrorCoral,
    shadowBubble          = ShadowBubble,
    reflectHighlight      = ReflectWhite,
    geminiAccent          = GeminiVioletLight,
    isDark                = false,
)

val LocalYouMeColors = staticCompositionLocalOf { LightYouMeColors }

// ─────────────────────────────────────────────────────────────
//  Main theme composable
// ─────────────────────────────────────────────────────────────

@Composable
fun YouMeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val youMeColors = if (darkTheme) DarkYouMeColors else LightYouMeColors

    CompositionLocalProvider(LocalYouMeColors provides youMeColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = YouMeTypography,
            shapes      = YouMeShapes,
            content     = content,
        )
    }
}

/** Shortcut extension */
val MaterialTheme.youMeColors: YouMeColors
    @Composable get() = LocalYouMeColors.current
