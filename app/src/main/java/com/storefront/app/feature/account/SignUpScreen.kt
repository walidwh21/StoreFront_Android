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
import com.storefront.app.core.util.MIN_PASSWORD_LENGTH
import com.storefront.app.core.util.isValidEmail

@Composable
fun SignUpScreen(
    state: AccountUiState,
    onSubmit: (fullName: String, email: String, password: String) -> Unit,
    onSwitchToSignIn: () -> Unit,
    onSignedIn: () -> Unit
) {
    val context = LocalContext.current
    var fullName by rememberSaveable { mutableStateOf("") }
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
        Text("Create your account", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        FormField(fullName, { fullName = it }, "Full name")
        Spacer(Modifier.height(12.dp))
        FormField(email, { email = it }, "Email", keyboardType = KeyboardType.Email)
        Spacer(Modifier.height(12.dp))
        FormField(password, { password = it }, "Password (min. $MIN_PASSWORD_LENGTH characters)", isPassword = true)
        Spacer(Modifier.height(24.dp))

        Button(
            enabled = !state.loading,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val problem = when {
                    fullName.isBlank() || email.isBlank() || password.isBlank() -> "Please fill in every field"
                    !email.isValidEmail() -> "That email address doesn't look right"
                    password.length < MIN_PASSWORD_LENGTH -> "Password must be at least $MIN_PASSWORD_LENGTH characters"
                    else -> null
                }
                if (problem != null) {
                    Toast.makeText(context, problem, Toast.LENGTH_SHORT).show()
                } else {
                    onSubmit(fullName, email, password)
                }
            }
        ) {
            Text(if (state.loading) "Creating account…" else "Sign up")
        }

        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onSwitchToSignIn) { Text("Already registered? Sign in") }
    }
}
