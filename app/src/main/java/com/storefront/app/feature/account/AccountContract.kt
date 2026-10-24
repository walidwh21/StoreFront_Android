package com.storefront.app.feature.account

import com.storefront.app.domain.model.User
import com.storefront.app.domain.model.UserRole

data class AccountUiState(
    /** The signed-in user, or null when browsing as a guest. */
    val currentUser: User? = null,
    /** The profile currently being viewed or edited (may be someone else's, for admins). */
    val profile: User? = null,
    val users: List<User> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    /** Raised once after a successful profile save; acknowledged by the UI. */
    val profileSaved: Boolean = false
) {
    val isSignedIn: Boolean get() = currentUser != null
    val isAdmin: Boolean get() = currentUser?.role == UserRole.ADMIN
}

sealed interface AccountAction {
    data class SignIn(val email: String, val password: String) : AccountAction
    data class SignUp(val fullName: String, val email: String, val password: String) : AccountAction
    data object SignOut : AccountAction
    data class LoadProfile(val userId: String) : AccountAction
    data object LoadUsers : AccountAction
    data class SaveProfile(val user: User) : AccountAction
    data object AckSaved : AccountAction
    data object DismissError : AccountAction
}
