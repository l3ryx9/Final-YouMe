package com.youme24.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────────
//  Shape scale — reprend BORDER_RADIUS de theme.ts
//  sm=8, md=12, lg=16, xl=24, round=50, bubble=18
// ─────────────────────────────────────────────────────────────

val YouMeShapes = Shapes(
    // Small chips, badges, tooltips
    extraSmall = RoundedCornerShape(8.dp),
    // Buttons, inputs, cards
    small      = RoundedCornerShape(12.dp),
    // Bottom sheets, dialogs
    medium     = RoundedCornerShape(16.dp),
    // Large modals, drawers
    large      = RoundedCornerShape(24.dp),
    // Full round (avatars, FAB)
    extraLarge = RoundedCornerShape(50.dp),
)

// Named shapes for specific use-cases
val BubbleShape      = RoundedCornerShape(18.dp)    // chat bubbles
val AvatarShape      = RoundedCornerShape(50.dp)    // circular avatars
val CardShape        = RoundedCornerShape(16.dp)    // content cards
val ButtonShape      = RoundedCornerShape(12.dp)    // buttons
val InputShape       = RoundedCornerShape(12.dp)    // text fields
val BottomSheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)

// Chat bubble corners (own message vs partner)
val BubbleOwnShape = RoundedCornerShape(
    topStart     = 18.dp,
    topEnd       = 18.dp,
    bottomStart  = 18.dp,
    bottomEnd    = 4.dp,   // flat corner points to sender
)
val BubbleOtherShape = RoundedCornerShape(
    topStart     = 18.dp,
    topEnd       = 18.dp,
    bottomStart  = 4.dp,
    bottomEnd    = 18.dp,
)
