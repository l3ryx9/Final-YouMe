package com.youme24.app.ui.partners

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.youme24.app.ui.components.Avatar
import com.youme24.app.ui.components.ThemedAlert
import com.youme24.app.ui.theme.Spacing

/**
 * Écran partenaires — équivalent de app/(app)/(tabs)/partners.tsx.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnersScreen(
    viewModel: PartnersViewModel = hiltViewModel(),
    onOpenFlags: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Partenaires") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            // Error/success banners
            if (uiState.error != null || uiState.successMessage != null) {
                item {
                    (uiState.error ?: uiState.successMessage)?.let { msg ->
                        ThemedAlert(message = msg, onDismiss = viewModel::clearMessages)
                    }
                }
            }

            // Pending requests section
            if (uiState.pendingRequests.isNotEmpty()) {
                item {
                    Text("Demandes reçues", style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary)
                }
                items(uiState.pendingRequests, key = { it.id }) { request ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(Spacing.md),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Avatar(photoUrl = request.fromPhotoUrl, size = 44.dp)
                            Spacer(Modifier.width(Spacing.sm))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(request.fromDisplayName, style = MaterialTheme.typography.titleSmall)
                                Text("@${request.fromUsername}", style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { viewModel.acceptRequest(request.id) }) {
                                Icon(Icons.Outlined.Check, "Accepter", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { viewModel.rejectRequest(request.id) }) {
                                Icon(Icons.Outlined.Close, "Refuser", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            // Partners section
            if (uiState.partners.isNotEmpty()) {
                item {
                    Text("Mes partenaires", style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary)
                }
                items(uiState.partners, key = { it.partnerId }) { partner ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(Spacing.md),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box {
                                Avatar(photoUrl = partner.partnerPhotoUrl, size = 48.dp)
                                if (partner.partnerIsOnline) {
                                    Box(
                                        Modifier
                                            .size(12.dp)
                                            .align(Alignment.BottomEnd)
                                            .then(
                                                Modifier.offset(2.dp, 2.dp)
                                            )
                                    )
                                }
                            }
                            Spacer(Modifier.width(Spacing.sm))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(partner.partnerDisplayName, style = MaterialTheme.typography.titleSmall)
                                Text("@${partner.partnerUsername}", style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(if (partner.partnerIsOnline) "En ligne" else "Hors ligne",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (partner.partnerIsOnline)
                                        MaterialTheme.youMeColors.onlineIndicator
                                    else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { onOpenFlags(partner.partnerId) }) {
                                Icon(Icons.Outlined.Flag, "Voir les flags")
                            }
                        }
                    }
                }
            }

            if (uiState.partners.isEmpty() && uiState.pendingRequests.isEmpty()) {
                item {
                    Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.Favorite, null,
                                Modifier.size(64.dp), MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(Spacing.sm))
                            Text("Aucun partenaire pour l'instant",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Utilisez la recherche pour trouver quelqu'un.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
