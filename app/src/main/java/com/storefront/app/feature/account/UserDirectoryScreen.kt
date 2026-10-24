package com.storefront.app.feature.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.storefront.app.core.ui.EmptyView
import com.storefront.app.core.ui.ErrorView
import com.storefront.app.core.ui.LoadingView
import com.storefront.app.domain.model.User

/** Admin-only list of every registered account. */
@Composable
fun UserDirectoryScreen(
    state: AccountUiState,
    onLoad: () -> Unit,
    onOpenUser: (User) -> Unit
) {
    LaunchedEffect(Unit) { onLoad() }

    when {
        state.loading && state.users.isEmpty() -> LoadingView()
        state.error != null && state.users.isEmpty() -> ErrorView(state.error, onRetry = onLoad)
        state.users.isEmpty() -> EmptyView("No registered users yet")
        else -> LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.users, key = { it.userId }) { user ->
                Card(
                    onClick = { onOpenUser(user) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(user.fullName, style = MaterialTheme.typography.titleMedium)
                        Text(user.email, style = MaterialTheme.typography.bodySmall)
                        Text(
                            user.role.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
