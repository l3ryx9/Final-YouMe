package com.youme24.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.youme24.app.ui.components.ThemedAlert
import com.youme24.app.ui.theme.Spacing

/**
 * Écran paramètres — équivalent de app/(app)/(tabs)/settings.tsx.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onLogout: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoggedOut) { if (uiState.isLoggedOut) onLogout() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Paramètres") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            // Profile card
            uiState.currentUser?.let { user ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(Spacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Outlined.AccountCircle, null, Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(Spacing.sm))
                        Column {
                            Text(user.displayName, style = MaterialTheme.typography.titleMedium)
                            Text("@${user.username}", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(user.email, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(Modifier.height(Spacing.sm))
            Text("Apparence", style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary)

            // Dark mode toggle
            SettingsSwitchRow(
                icon = Icons.Outlined.DarkMode,
                title = "Mode sombre",
                checked = uiState.isDarkMode,
                onCheckedChange = viewModel::setDarkMode,
            )

            Spacer(Modifier.height(Spacing.sm))
            Text("Intelligence artificielle", style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary)

            // AI toggle
            SettingsSwitchRow(
                icon = Icons.Outlined.AutoAwesome,
                title = "Analyse IA activée",
                subtitle = "Analyse les messages avec Gemini",
                checked = uiState.aiEnabled,
                onCheckedChange = viewModel::setAiEnabled,
            )

            Spacer(Modifier.height(Spacing.sm))
            Text("Notifications", style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary)

            SettingsSwitchRow(
                icon = Icons.Outlined.Notifications,
                title = "Notifications push",
                checked = uiState.notificationsEnabled,
                onCheckedChange = viewModel::setNotificationsEnabled,
            )

            Spacer(Modifier.height(Spacing.md))
            HorizontalDivider()
            Spacer(Modifier.height(Spacing.sm))

            // Logout
            OutlinedButton(
                onClick = { viewModel.logout() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Icon(Icons.Outlined.Logout, null)
                Spacer(Modifier.width(Spacing.sm))
                Text("Se déconnecter")
            }

            // Delete account (RGPD)
            TextButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Icon(Icons.Outlined.DeleteForever, null)
                Spacer(Modifier.width(Spacing.sm))
                Text("Supprimer mon compte")
            }

            uiState.error?.let {
                ThemedAlert(message = it, onDismiss = viewModel::clearError)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer le compte") },
            text  = { Text("Cette action est irréversible. Toutes vos données seront supprimées conformément au RGPD.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteAccount(); showDeleteDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Supprimer définitivement") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Annuler") }
            },
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium)
                subtitle?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
