package com.youme24.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.youme24.app.ui.theme.*

/**
 * Bouton 3D "Pineapple Paradise" — reprend et étend Bubble3DButton.tsx.
 *
 * Techno animations : Compose natif uniquement.
 *  - animateDpAsState  → ombre qui se réduit à l'état pressé (effet "enfoncement")
 *  - animateFloatAsState → translation Y (le bouton descend légèrement)
 *  - Brush.radialGradient → dégradé radial clair → foncé (identité 3D)
 *  - drawBehind → ombre portée large en bas (shadowElevation custom)
 *  - Reflet elliptique semi-transparent en haut du bouton (Box avec Brush vertical)
 *
 * Équivalents React Native → Kotlin :
 *   SHADOW.bubble          → Elevation.bubble (16.dp)
 *   SHADOW.bubblePressed   → 2.dp (état pressé)
 */
@Composable
fun Bubble3DButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    accentColor: Color = OrangeAnanas,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Enfoncement au clic — translation Y + réduction ombre
    val shadowElevation by animateDpAsState(
        targetValue = if (isPressed) Elevation.low else Elevation.bubble,
        animationSpec = tween(100),
        label = "shadowElevation",
    )
    val translateY by animateFloatAsState(
        targetValue = if (isPressed) 4f else 0f,
        animationSpec = tween(100),
        label = "translateY",
    )

    val shadowColor = ShadowBubble

    Box(
        modifier = modifier
            .height(52.dp)
            .clip(ButtonShape)
            .drawBehind {
                // Ombre portée 3D en bas
                val offsetY = size.height * 0.08f
                drawRoundRect(
                    color        = shadowColor,
                    topLeft      = androidx.compose.ui.geometry.Offset(
                        x = shadowElevation.toPx() * 0.5f,
                        y = size.height - offsetY + shadowElevation.toPx() * 0.3f,
                    ),
                    size         = androidx.compose.ui.geometry.Size(
                        width  = size.width - shadowElevation.toPx(),
                        height = shadowElevation.toPx() * 1.5f,
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(ButtonShape.topStart.toPx(
                        androidx.compose.ui.geometry.Size(size.width, size.height),
                        this
                    )),
                )
            }
            .graphicsLayer { translationY = translateY }
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.95f),
                        accentColor,
                    ),
                    radius = 600f,
                )
            )
            .semantics { role = Role.Button }
            .pointerInput(enabled) {
                if (!enabled || isLoading) return@pointerInput
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == androidx.compose.ui.input.pointer.PointerEventType.Release) {
                            onClick()
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        // Reflet elliptique semi-transparent en haut
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(14.dp)
                .align(Alignment.TopCenter)
                .offset(y = 4.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(ReflectWhite, Color.Transparent)
                    )
                )
        )

        // Content
        if (isLoading) {
            CircularProgressIndicator(
                color    = Color.White,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.5.dp,
            )
        } else {
            Text(
                text  = text,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
            )
        }
    }
}

// Convenience shorthand using clickable + the button above as Composable
@Composable
fun Bubble3DIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = BubbleSize.md,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable BoxScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val shadowElevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 8.dp,
        animationSpec = tween(100), label = "iconShadow",
    )
    val translateY by animateFloatAsState(
        targetValue = if (isPressed) 3f else 0f,
        animationSpec = tween(100), label = "iconTranslateY",
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer { translationY = translateY }
            .clip(androidx.compose.foundation.shape.CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.9f), OrangeDark)
                )
            )
            .semantics { role = Role.Button }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == androidx.compose.ui.input.pointer.PointerEventType.Release) {
                            onClick()
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center,
        content = content,
    )
}
