package com.youme24.app.ui.flags

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.youme24.app.domain.model.FlagType
import com.youme24.app.domain.model.RelationalFlag
import com.youme24.app.ui.theme.ErrorCoral
import com.youme24.app.ui.theme.OnlineGreen
import com.youme24.app.ui.theme.Spacing

/**
 * Écran flags relationnels — équivalent de app/(app)/flags/[id].tsx.
 *
 * Techno animations : AnimatedVisibility + staggered enter pour chaque flag (Compose natif).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlagsScreen(
    partnerId: String,
    viewModel: FlagsViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(partnerId) { viewModel.init(partnerId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Signaux relationnels") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Retour") }
                },
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                val redFlags   = uiState.flags.filter { it.type == FlagType.RED_FLAG }
                val greenFlags = uiState.flags.filter { it.type == FlagType.GREEN_FLAG }

                if (greenFlags.isNotEmpty()) {
                    item {
                        Text("🟢 Green Flags", style = MaterialTheme.typography.titleSmall,
                            color = OnlineGreen)
                    }
                    items(greenFlags, key = { it.id }) { flag -> FlagCard(flag) }
                }
                if (redFlags.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(Spacing.sm))
                        Text("🔴 Red Flags", style = MaterialTheme.typography.titleSmall,
                            color = ErrorCoral)
                    }
                    items(redFlags, key = { it.id }) { flag -> FlagCard(flag) }
                }
                if (uiState.flags.isEmpty()) {
                    item {
                        Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Outlined.Flag, null, Modifier.size(56.dp),
                                    MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(Spacing.sm))
                                Text("Aucun signal détecté",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlagCard(flag: RelationalFlag) {
    val containerColor = if (flag.type == FlagType.GREEN_FLAG)
        OnlineGreen.copy(alpha = 0.12f) else ErrorCoral.copy(alpha = 0.12f)
    val contentColor = if (flag.type == FlagType.GREEN_FLAG) OnlineGreen else ErrorCoral

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (flag.type == FlagType.GREEN_FLAG) "🟢" else "🔴")
                Spacer(Modifier.width(Spacing.xs))
                Text(flag.label, style = MaterialTheme.typography.titleSmall, color = contentColor)
                Spacer(Modifier.weight(1f))
                repeat(flag.severity.coerceIn(1, 5)) {
                    Text("★", style = MaterialTheme.typography.labelSmall, color = contentColor)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(flag.description, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface)
            flag.citation?.let {
                Spacer(Modifier.height(4.dp))
                Text("\"$it\"", style = MaterialTheme.typography.bodySmall.copy(
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
