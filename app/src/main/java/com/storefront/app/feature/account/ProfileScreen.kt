package com.storefront.app.feature.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.storefront.app.core.format.asDateTime
import com.storefront.app.core.ui.ErrorView
import com.storefront.app.core.ui.LoadingView

@Composable
fun ProfileScreen(
    userId: String,
    state: AccountUiState,
    onLoad: (String) -> Unit,
    onEdit: () -> Unit,
    onSignOut: () -> Unit
) {
    LaunchedEffect(userId) { onLoad(userId) }

    val profile = state.profile
    val isSelf = state.currentUser?.userId == userId
    val canEdit = isSelf || state.isAdmin

    when {
        profile != null && profile.userId == userId -> Column(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            Text(
                if (isSelf) "Your profile" else "Customer profile",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    InfoRow(Icons.Default.Person, "Name", profile.fullName)
                    InfoRow(Icons.Default.Email, "Email", profile.email)
                    InfoRow(Icons.Default.Phone, "Phone", profile.phoneNumber?.takeIf { it.isNotBlank() } ?: "Not set")
                    InfoRow(
                        Icons.Default.Home, "Address",
                        profile.address?.oneLine()?.takeIf { it.isNotBlank() } ?: "Not set"
                    )
                    InfoRow(Icons.Default.Lock, "Role", profile.role.name)
                    InfoRow(Icons.Default.DateRange, "Member since", profile.createdAt.asDateTime())
                }
            }

            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (canEdit) {
                    Button(onClick = onEdit, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Edit")
                    }
                }
                if (isSelf) {
                    Button(
                        onClick = onSignOut,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Sign out")
                    }
                }
            }
        }

        state.loading -> LoadingView()
        else -> ErrorView(state.error ?: "Profile not found", onRetry = { onLoad(userId) })
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
