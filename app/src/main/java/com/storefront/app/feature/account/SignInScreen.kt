package com.storefront.app.feature.account

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.storefront.app.core.ui.FormField
import com.storefront.app.core.util.isValidEmail

@Composable
fun SignInScreen(
    state: AccountUiState,
    onSubmit: (email: String, password: String) -> Unit,
    onSwitchToSignUp: () -> Unit,
    onSignedIn: () -> Unit
) {
    val context = LocalContext.current
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(state.isSignedIn) {
        if (state.isSignedIn) onSignedIn()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome back", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Sign in to check out and track your orders",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))

        FormField(email, { email = it }, "Email", keyboardType = KeyboardType.Email)
        Spacer(Modifier.height(12.dp))
        FormField(password, { password = it }, "Password", isPassword = true)
        Spacer(Modifier.height(24.dp))

        Button(
            enabled = !state.loading,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                when {
                    email.isBlank() || password.isBlank() ->
                        Toast.makeText(context, "Enter your email and password", Toast.LENGTH_SHORT).show()
                    !email.isValidEmail() ->
                        Toast.makeText(context, "That email address doesn't look right", Toast.LENGTH_SHORT).show()
                    else -> onSubmit(email.trim(), password)
                }
            }
        ) {
            Text(if (state.loading) "Signing in…" else "Sign in")
        }

        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onSwitchToSignUp) { Text("New here? Create an account") }
    }
}
