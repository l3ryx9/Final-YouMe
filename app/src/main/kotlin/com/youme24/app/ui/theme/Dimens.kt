package com.youme24.app.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────────
//  Design tokens — reprend theme.ts SPACING / BUBBLE_SIZES
// ─────────────────────────────────────────────────────────────

object Spacing {
    val xs   = 4.dp
    val sm   = 8.dp
    val md   = 16.dp
    val lg   = 24.dp
    val xl   = 32.dp
    val xxl  = 48.dp
}

object BubbleSize {
    /** Taille du bouton IA flottant (petit) */
    val sm   = 48.dp
    /** Bouton d'action standard */
    val md   = 60.dp
    /** Bouton principal / FAB */
    val lg   = 76.dp
    /** Mascotte / illustration animée */
    val xl   = 96.dp
}

object AvatarSize {
    val sm   = 32.dp
    val md   = 44.dp
    val lg   = 56.dp
    val xl   = 80.dp
}

object IconSize {
    val sm   = 16.dp
    val md   = 24.dp
    val lg   = 32.dp
}

object Elevation {
    val none   = 0.dp
    val low    = 2.dp
    val medium = 6.dp
    val high   = 12.dp
    /** Ombre 3D pour Bubble3DButton */
    val bubble = 16.dp
}
