package com.youme24.app.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.youme24.app.ui.components.AnimatedYouMeLogo
import com.youme24.app.ui.components.Bubble3DButton
import com.youme24.app.ui.components.PasswordStrengthBar
import com.youme24.app.ui.components.PineapplePatternBackground
import com.youme24.app.ui.components.SimpleCaptcha
import com.youme24.app.ui.components.ThemedAlert
import com.youme24.app.ui.theme.Spacing

/**
 * Écran d'inscription — équivalent de app/(auth)/register.tsx.
 *
 * Composants spécifiques : PasswordStrengthBar (Compose natif),
 * SimpleCaptcha (Compose natif), PineapplePatternBackground (Canvas).
 */
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onRegisterSuccess: () -> Unit,
    onNavigateLogin: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    var email        by remember { mutableStateOf("") }
    var username     by remember { mutableStateOf("") }
    var displayName  by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var confirmPass  by remember { mutableStateOf("") }
    var showPass     by remember { mutableStateOf(false) }
    var captchaValid by remember { mutableStateOf(false) }
    var formError    by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onRegisterSuccess()
    }

    val passwordsMatch = password == confirmPass || confirmPass.isEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PineapplePatternBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md, vertical = Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(Spacing.lg))
            AnimatedYouMeLogo(size = 80.dp)
            Spacer(Modifier.height(Spacing.sm))

            Text(
                text  = "Créer un compte",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(Spacing.xl))

            OutlinedTextField(
                value = displayName, onValueChange = { displayName = it },
                label = { Text("Prénom ou surnom") },
                leadingIcon = { Icon(Icons.Outlined.Person, null) },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )
            Spacer(Modifier.height(Spacing.sm))

            OutlinedTextField(
                value = username, onValueChange = { username = it.lowercase().trim() },
                label = { Text("Nom d'utilisateur (@username)") },
                leadingIcon = { Icon(Icons.Outlined.AlternateEmail, null) },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )
            Spacer(Modifier.height(Spacing.sm))

            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Outlined.Email, null) },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email, imeAction = ImeAction.Next
                ),
            )
            Spacer(Modifier.height(Spacing.sm))

            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Mot de passe") },
                leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { showPass = !showPass }) {
                        Icon(if (showPass) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, null)
                    }
                },
                visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password, imeAction = ImeAction.Next
                ),
            )

            // Indicateur de force du mot de passe (Compose natif)
            AnimatedVisibility(visible = password.isNotEmpty()) {
                PasswordStrengthBar(password = password, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
            }

            Spacer(Modifier.height(Spacing.sm))

            OutlinedTextField(
                value = confirmPass, onValueChange = { confirmPass = it },
                label = { Text("Confirmer le mot de passe") },
                leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                isError = !passwordsMatch,
                supportingText = { if (!passwordsMatch) Text("Les mots de passe ne correspondent pas") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password, imeAction = ImeAction.Done
                ),
            )

            Spacer(Modifier.height(Spacing.md))

            // Captcha anti-bot (Compose natif — SimpleCaptcha)
            SimpleCaptcha(
                onValidated = { captchaValid = it },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Spacing.md))

            AnimatedVisibility(visible = (uiState.error ?: formError) != null) {
                val msg = uiState.error ?: formError ?: ""
                ThemedAlert(message = msg, onDismiss = { viewModel.clearError(); formError = null })
                Spacer(Modifier.height(Spacing.sm))
            }

            Bubble3DButton(
                text = "Créer mon compte",
                onClick = {
                    if (!passwordsMatch) { formError = "Les mots de passe ne correspondent pas"; return@Bubble3DButton }
                    if (!captchaValid) { formError = "Validez le captcha"; return@Bubble3DButton }
                    viewModel.register(email, password, username, displayName)
                },
                isLoading = uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Spacing.md))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Déjà un compte ?")
                TextButton(onClick = onNavigateLogin) { Text("Se connecter") }
            }

            Spacer(Modifier.height(Spacing.xxl))
        }
    }
}
