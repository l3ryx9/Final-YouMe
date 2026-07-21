package com.youme24.app.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.youme24.app.domain.model.User
import com.youme24.app.ui.components.Avatar
import com.youme24.app.ui.theme.Spacing

/**
 * Écran recherche utilisateurs — équivalent de app/(app)/(tabs)/search.tsx.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: SearchViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var query by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Recherche") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Spacing.md),
        ) {
            OutlinedTextField(
                value         = query,
                onValueChange = { query = it; viewModel.search(it) },
                placeholder   = { Text("Rechercher par @username…") },
                leadingIcon   = { Icon(Icons.Outlined.Search, null) },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                shape         = MaterialTheme.shapes.extraLarge,
            )
            Spacer(Modifier.height(Spacing.md))

            if (uiState.isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    items(uiState.results, key = { it.id }) { user ->
                        SearchUserItem(
                            user     = user,
                            onAdd    = { viewModel.sendRequest(user.id) },
                            isAdding = uiState.sendingTo == user.id,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchUserItem(user: User, onAdd: () -> Unit, isAdding: Boolean) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(photoUrl = user.photoUrl, size = 44.dp)
            Spacer(Modifier.width(Spacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.displayName, style = MaterialTheme.typography.titleSmall)
                Text("@${user.username}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onAdd, enabled = !isAdding) {
                if (isAdding) CircularProgressIndicator(Modifier.size(24.dp))
                else Icon(Icons.Outlined.PersonAdd, "Envoyer une demande",
                    tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
