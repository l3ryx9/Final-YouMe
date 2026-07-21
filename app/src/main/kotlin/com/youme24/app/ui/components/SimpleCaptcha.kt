package com.youme24.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.youme24.app.ui.theme.OnlineGreen
import com.youme24.app.ui.theme.Spacing
import kotlin.random.Random

/**
 * Captcha anti-bot simple — équivalent de SimpleCaptcha.tsx.
 *
 * Implémentation : math challenge (a + b = ?) ou case à cocher stylisée.
 * Remplace expo-captcha et reproduit le comportement de antiBot.ts.
 *
 * Techno animations : animateColorAsState (bordure verte/grise selon état — Compose natif).
 */
@Composable
fun SimpleCaptcha(
    onValidated: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var a by remember { mutableStateOf(Random.nextInt(1, 10)) }
    var b by remember { mutableStateOf(Random.nextInt(1, 10)) }
    var answer   by remember { mutableStateOf("") }
    var validated by remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        targetValue   = if (validated) OnlineGreen else MaterialTheme.colorScheme.outline,
        animationSpec = tween(300),
        label         = "captchaBorder",
    )

    fun refresh() {
        a = Random.nextInt(1, 10)
        b = Random.nextInt(1, 10)
        answer = ""
        validated = false
        onValidated(false)
    }

    fun check() {
        val correct = answer.trim().toIntOrNull() == a + b
        validated = correct
        onValidated(correct)
    }

    Card(
        modifier = modifier
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.medium,
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            // Math challenge
            Text(
                text  = "Je ne suis pas un robot",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f),
            )

            if (!validated) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text("$a + $b =", fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                    OutlinedTextField(
                        value         = answer,
                        onValueChange = { answer = it; if (it.isNotBlank()) check() },
                        modifier      = Modifier.width(56.dp),
                        singleLine    = true,
                        textStyle     = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                    )
                }
                IconButton(onClick = ::refresh) {
                    Icon(Icons.Outlined.Refresh, "Recharger", Modifier.size(18.dp))
                }
            } else {
                Icon(
                    Icons.Outlined.CheckCircle, "Validé",
                    Modifier.size(28.dp), tint = OnlineGreen,
                )
                Text("Validé", style = MaterialTheme.typography.labelMedium, color = OnlineGreen)
            }
        }
    }
}
