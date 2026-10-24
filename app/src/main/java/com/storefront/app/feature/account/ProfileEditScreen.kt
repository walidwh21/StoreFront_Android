package com.storefront.app.feature.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.storefront.app.core.ui.DropdownField
import com.storefront.app.core.ui.ErrorView
import com.storefront.app.core.ui.FormField
import com.storefront.app.core.ui.LoadingView
import com.storefront.app.domain.model.Address
import com.storefront.app.domain.model.User
import com.storefront.app.domain.model.UserRole

/**
 * Editable profile form. Everyone can change their own contact details;
 * only admins can change an e-mail address or a role.
 */
@Composable
fun ProfileEditScreen(
    userId: String,
    state: AccountUiState,
    onLoad: (String) -> Unit,
    onSave: (User) -> Unit,
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    LaunchedEffect(userId) { onLoad(userId) }
    LaunchedEffect(state.profileSaved) {
        if (state.profileSaved) onSaved()
    }

    val profile = state.profile
    if (profile == null || profile.userId != userId) {
        if (state.loading) LoadingView() else ErrorView(state.error ?: "Profile not found", onRetry = { onLoad(userId) })
        return
    }

    // Keyed on userId so switching profiles never shows stale text.
    var fullName by rememberSaveable(profile.userId) { mutableStateOf(profile.fullName) }
    var email by rememberSaveable(profile.userId) { mutableStateOf(profile.email) }
    var phone by rememberSaveable(profile.userId) { mutableStateOf(profile.phoneNumber.orEmpty()) }
    var street by rememberSaveable(profile.userId) { mutableStateOf(profile.address?.street.orEmpty()) }
    var city by rememberSaveable(profile.userId) { mutableStateOf(profile.address?.city.orEmpty()) }
    var postalCode by rememberSaveable(profile.userId) { mutableStateOf(profile.address?.postalCode.orEmpty()) }
    var country by rememberSaveable(profile.userId) { mutableStateOf(profile.address?.country.orEmpty()) }
    var role by rememberSaveable(profile.userId) { mutableStateOf(profile.role) }

    val isAdmin = state.isAdmin

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Personal details", style = MaterialTheme.typography.titleMedium)
        FormField(fullName, { fullName = it }, "Full name")
        if (isAdmin) {
            FormField(email, { email = it }, "Email", keyboardType = KeyboardType.Email)
        }
        FormField(phone, { phone = it }, "Phone number", keyboardType = KeyboardType.Phone)

        Spacer(Modifier.height(8.dp))
        Text("Delivery address", style = MaterialTheme.typography.titleMedium)
        FormField(street, { street = it }, "Street")
        FormField(city, { city = it }, "City")
        FormField(postalCode, { postalCode = it }, "Postal code", keyboardType = KeyboardType.Number)
        FormField(country, { country = it }, "Country")

        if (isAdmin) {
            Spacer(Modifier.height(8.dp))
            Text("Access", style = MaterialTheme.typography.titleMedium)
            DropdownField(
                label = "Role",
                selected = role,
                options = UserRole.entries,
                display = { it.name },
                onSelect = { it?.let { picked -> role = picked } }
            )
        }

        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
        ) {
            OutlinedButton(onClick = onBack) { Text("Cancel") }
            Button(
                enabled = !state.loading && fullName.isNotBlank(),
                onClick = {
                    onSave(
                        profile.copy(
                            fullName = fullName.trim(),
                            // Non-admins can't change these two; keep what's stored.
                            email = if (isAdmin) email.trim() else profile.email,
                            role = if (isAdmin) role else profile.role,
                            phoneNumber = phone.trim().ifBlank { null },
                            address = Address(street.trim(), city.trim(), postalCode.trim().ifBlank { null }, country.trim())
                        )
                    )
                }
            ) {
                Text(if (state.loading) "Saving…" else "Save")
            }
        }
    }
}
