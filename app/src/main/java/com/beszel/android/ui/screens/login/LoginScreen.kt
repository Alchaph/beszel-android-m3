package com.beszel.android.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beszel.android.data.model.Session

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onSessionReady: (Session) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(64.dp))

        // Brand mark
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(72.dp),
            tonalElevation = 2.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Storage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(36.dp),
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Sign in", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(6.dp))
        Text(
            text = when (state.step) {
                LoginStep.Server      -> "Connect to your Beszel hub"
                LoginStep.Credentials -> "Continue to ${runCatching { java.net.URL(state.serverUrl).host }.getOrElse { state.serverUrl }}"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(40.dp))

        when (state.step) {
            LoginStep.Server -> ServerStep(state, viewModel)
            LoginStep.Credentials -> CredentialsStep(state, viewModel, onSessionReady)
        }

        Spacer(Modifier.weight(1f))
        Text(
            "Beszel · v0.8+",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 24.dp),
        )
    }
}

@Composable
private fun ServerStep(state: LoginUiState, vm: LoginViewModel) {
    OutlinedTextField(
        value = state.serverUrl,
        onValueChange = vm::setServerUrl,
        label = { Text("Server URL") },
        placeholder = { Text("https://hub.example.com") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Go,
        ),
        keyboardActions = KeyboardActions(onGo = { vm.connectToServer() }),
        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
    )
    Spacer(Modifier.height(12.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = state.trustSelfSigned,
            onCheckedChange = vm::setTrustSelfSigned,
        )
        Spacer(Modifier.width(4.dp))
        Column {
            Text("Trust self-signed certificate", style = MaterialTheme.typography.bodyMedium)
            if (state.trustSelfSigned) {
                Text(
                    "Connection is not verified. Use only on trusted networks.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
    Spacer(Modifier.height(16.dp))

    Button(
        onClick = { vm.connectToServer() },
        modifier = Modifier.fillMaxWidth(),
        enabled = !state.loading,
    ) {
        if (state.loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Spacer(Modifier.width(8.dp))
            Text("Connecting…")
        } else {
            Text("Continue")
        }
    }

    if (state.error != null) {
        Spacer(Modifier.height(12.dp))
        ErrorBanner(state.error)
    }

    Spacer(Modifier.height(24.dp))
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(
                Icons.Default.Info, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp).padding(top = 2.dp),
            )
            Text(
                "Your hub address is the URL where Beszel is reachable — e.g. https://beszel.example.com or via Tailscale.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CredentialsStep(
    state: LoginUiState,
    vm: LoginViewModel,
    onSessionReady: (Session) -> Unit,
) {
    val passwordFocus = remember { FocusRequester() }

    OutlinedTextField(
        value = state.email,
        onValueChange = vm::setEmail,
        label = { Text("Email") },
        placeholder = { Text("you@example.com") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
        ),
        keyboardActions = KeyboardActions(onNext = { passwordFocus.requestFocus() }),
    )
    Spacer(Modifier.height(12.dp))

    OutlinedTextField(
        value = state.password,
        onValueChange = vm::setPassword,
        label = { Text("Password") },
        modifier = Modifier.fillMaxWidth().focusRequester(passwordFocus),
        singleLine = true,
        visualTransformation = if (state.showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(onDone = { vm.signIn(onSessionReady) }),
        trailingIcon = {
            IconButton(onClick = vm::toggleShowPassword) {
                Icon(
                    if (state.showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (state.showPassword) "Hide password" else "Show password",
                )
            }
        },
    )
    Spacer(Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = vm::goBackToServer) { Text("Change server") }
    }
    Spacer(Modifier.height(8.dp))

    Button(
        onClick = { vm.signIn(onSessionReady) },
        modifier = Modifier.fillMaxWidth(),
        enabled = !state.loading,
    ) {
        if (state.loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Spacer(Modifier.width(8.dp))
            Text("Signing in…")
        } else {
            Text("Sign in")
        }
    }

    if (state.error != null) {
        Spacer(Modifier.height(12.dp))
        ErrorBanner(state.error)
    }

}

@Composable
private fun ErrorBanner(message: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(18.dp),
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}
