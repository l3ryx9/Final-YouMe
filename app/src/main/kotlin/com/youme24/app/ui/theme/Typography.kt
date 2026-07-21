package com.youme24.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.youme24.app.R

// ─────────────────────────────────────────────────────────────
//  Font families
//  Place the actual .ttf files in app/src/main/res/font/
//  ▸ dancing_script_bold.ttf   → logo / titre "YouMe"
//  ▸ nunito_regular.ttf        → corps de texte
//  ▸ nunito_semibold.ttf       → sous-titres
//  ▸ nunito_bold.ttf           → titres
// ─────────────────────────────────────────────────────────────

val DancingScriptFamily = FontFamily(
    Font(R.font.dancing_script_bold, FontWeight.Bold),
)

val NunitoFamily = FontFamily(
    Font(R.font.nunito_regular,  FontWeight.Normal),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold,     FontWeight.Bold),
)

// ─────────────────────────────────────────────────────────────
//  Typography scale
//  Tailles reprises de TYPOGRAPHY.size dans theme.ts :
//  xs=11, sm=13, md=15, lg=17, xl=20, xxl=24, heading=28
// ─────────────────────────────────────────────────────────────

val YouMeTypography = Typography(
    // Logo / marque — Dancing Script
    displayLarge = TextStyle(
        fontFamily = DancingScriptFamily,
        fontWeight = FontWeight.Bold,
        fontSize   = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
    ),
    // Section headings
    headlineLarge = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.Bold,
        fontSize   = 28.sp,          // heading
        lineHeight = 34.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.Bold,
        fontSize   = 24.sp,          // xxl
        lineHeight = 30.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 20.sp,          // xl
        lineHeight = 26.sp,
        letterSpacing = 0.sp,
    ),
    // Title (screen titles, card headers)
    titleLarge = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.Bold,
        fontSize   = 17.sp,          // lg
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 15.sp,          // md
        lineHeight = 22.sp,
        letterSpacing = 0.15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 13.sp,          // sm
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    // Body text
    bodyLarge = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 13.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 11.sp,          // xs
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    // Labels (buttons, chips)
    labelLarge = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp,
    ),
)
