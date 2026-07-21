package com.youme24.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.youme24.app.ui.theme.ErrorCoral
import com.youme24.app.ui.theme.Spacing

/**
 * Alerte contextuelle thémée — équivalent de ThemedAlert.tsx.
 *
 * Affiche un bandeau d'erreur / info avec bouton de fermeture.
 * Pas d'animation complexe — Material 3 Card avec couleur d'erreur.
 */
@Composable
fun ThemedAlert(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = true,
) {
    val containerColor = if (isError)
        ErrorCoral.copy(alpha = 0.12f)
    else
        MaterialTheme.colorScheme.primaryContainer
    val contentColor = if (isError) ErrorCoral else MaterialTheme.colorScheme.primary

    Card(
        modifier = modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Outlined.Error, null,
                Modifier.size(20.dp), tint = contentColor,
            )
            Spacer(Modifier.width(Spacing.sm))
            Text(
                text     = message,
                style    = MaterialTheme.typography.bodySmall,
                color    = contentColor,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick  = onDismiss,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(Icons.Outlined.Close, "Fermer", Modifier.size(16.dp), tint = contentColor)
            }
        }
    }
}
