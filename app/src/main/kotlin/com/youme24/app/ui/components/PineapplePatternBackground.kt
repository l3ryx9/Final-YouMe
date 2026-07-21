package com.youme24.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import com.youme24.app.ui.theme.PineappleYellow
import com.youme24.app.ui.theme.TropicalGreenLight

/**
 * Fond décoratif — motif ananas répété, faible opacité.
 * Équivalent de PineapplePattern.tsx / ForestPattern.tsx (React Native).
 *
 * Techno animations : Canvas Compose STATIQUE (pas de Lottie ici).
 * Pour une version animée en boucle très lente (scroll/flottement),
 * utiliser Lottie avec un fichier JSON d'ananas vectoriel.
 *
 * Pourquoi Canvas et pas Lottie ici ?
 *  - Le motif est simple (formes géométriques) → Canvas Compose = pas de dépendance asset.
 *  - Pour AnimatedYouMeLogo (mascotte complexe), Lottie est utilisé car l'animation
 *    est riche et difficile à reproduire en code Compose.
 */
@Composable
fun PineapplePatternBackground(
    modifier: Modifier = Modifier.fillMaxSize(),
    opacity: Float = 0.06f,
) {
    Canvas(modifier = modifier) {
        val cols = (size.width / 80f).toInt() + 2
        val rows = (size.height / 100f).toInt() + 2

        for (row in 0..rows) {
            for (col in 0..cols) {
                val offsetX = col * 80f + (if (row % 2 == 0) 0f else 40f)
                val offsetY = row * 100f
                drawPineapple(
                    center  = Offset(offsetX, offsetY),
                    scale   = 0.35f,
                    opacity = opacity,
                )
            }
        }
    }
}

private fun DrawScope.drawPineapple(center: Offset, scale: Float, opacity: Float) {
    val s = scale * 100f

    // Body (oval jaune)
    drawOval(
        color  = PineappleYellow.copy(alpha = opacity),
        topLeft = Offset(center.x - s * 0.35f, center.y - s * 0.55f),
        size   = androidx.compose.ui.geometry.Size(s * 0.7f, s * 1.1f),
    )

    // Crown (feuilles vertes)
    val leaf = Path().apply {
        moveTo(center.x, center.y - s * 0.55f)
        lineTo(center.x - s * 0.22f, center.y - s * 0.95f)
        lineTo(center.x, center.y - s * 0.75f)
        lineTo(center.x + s * 0.22f, center.y - s * 0.95f)
        close()
    }
    drawPath(path = leaf, color = TropicalGreenLight.copy(alpha = opacity))

    // Texture lines (diamants)
    val lineColor = Color(0xFFF2932E).copy(alpha = opacity * 0.7f)
    for (i in -2..2) {
        val y = center.y + i * s * 0.18f
        drawLine(
            color = lineColor,
            start = Offset(center.x - s * 0.3f, y),
            end   = Offset(center.x + s * 0.3f, y),
            strokeWidth = 1f,
        )
    }
}
