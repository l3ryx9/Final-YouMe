package com.youme24.app.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────
//  YouMe "Pineapple Paradise 3D" — palette complète
//  Remplace l'ancien thème "Forêt Enchantée / Clairière"
// ─────────────────────────────────────────────────────────────

// Primary — jaune ananas franc
val PineappleYellow      = Color(0xFFF4C63A)
val PineappleYellowDark  = Color(0xFFD4A020)
val PineappleYellowLight = Color(0xFFFFF0A0)

// Primary container / dark accent — orange ananas
val OrangeAnanas         = Color(0xFFF2932E)
val OrangeDark           = Color(0xFFC96A1E)
val OrangeLight          = Color(0xFFFFD49A)

// Secondary — vert feuille tropicale
val TropicalGreen        = Color(0xFF2E7D32)
val TropicalGreenLight   = Color(0xFF4C9A4C)
val TropicalGreenDark    = Color(0xFF1B5E20)

// Tertiary — rose corail / hibiscus
val HibiscusCoral        = Color(0xFFFF6F61)
val HibiscusLight        = Color(0xFFFF9E94)
val HibiscusDark         = Color(0xFFCC3D2E)

// Backgrounds
val NightTropical        = Color(0xFF0B1A1E)   // dark background
val CoconutCream         = Color(0xFFFFF8E7)   // light background

// Surfaces
val SurfaceDark          = Color(0xFF12241F)
val SurfaceLight         = Color(0xFFFFFFFF)

// Card / elevated surfaces (dark mode)
val CardDark             = Color(0xFF1A2F28)
val CardLight            = Color(0xFFF9F3E3)

// Chat bubbles
val BubbleOwn            = PineappleYellow          // "moi" — jaune ananas
val BubbleOwnGradientEnd = OrangeAnanas              // dégradé vers orange
val BubbleOtherDark      = TropicalGreen             // partenaire dark mode
val BubbleOtherLight     = Color(0xFFEAEAEA)         // partenaire light mode

// Status / states
val OnlineGreen          = Color(0xFF3DDC97)   // en ligne — vert lagon
val ReadOrange           = Color(0xFFF2932E)   // lu — orange ananas
val ErrorCoral           = Color(0xFFE0665A)   // erreur
val WarningYellow        = PineappleYellow     // avertissement

// Text
val TextOnDark           = Color(0xFFFFF8E7)
val TextOnLight          = Color(0xFF1A1A1A)
val TextSecondaryDark    = Color(0xFFB0B8A8)
val TextSecondaryLight   = Color(0xFF666666)

// Icon tint / accent
val AccentGold           = Color(0xFFFFCC00)
val AccentLagoon         = Color(0xFF00B4D8)

// Divider / outline
val OutlineDark          = Color(0xFF2A3D35)
val OutlineLight         = Color(0xFFDDD8C8)

// Shadows (used in Modifier.drawBehind for 3D effect)
val Shadow3D             = Color(0x66000000)
val ShadowBubble         = Color(0x33F2932E)  // orange shadow for 3D buttons
val ReflectWhite         = Color(0x99FFFFFF)  // top-highlight on 3D buttons

// AI / Gemini accent
val GeminiViolet         = Color(0xFF9C27B0)
val GeminiVioletLight    = Color(0xFFCE93D8)
