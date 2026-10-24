package com.storefront.app.feature.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storefront.app.core.util.attempt
import com.storefront.app.data.AccountRepository
import com.storefront.app.domain.model.User
import com.storefront.app.domain.model.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val repository: AccountRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AccountUiState())
    val state: StateFlow<AccountUiState> = _state.asStateFlow()

    init {
        restoreSession()
    }

    fun dispatch(action: AccountAction) {
        when (action) {
            is AccountAction.SignIn -> signIn(action.email, action.password)
            is AccountAction.SignUp -> signUp(action.fullName, action.email, action.password)
            AccountAction.SignOut -> signOut()
            is AccountAction.LoadProfile -> loadProfile(action.userId)
            AccountAction.LoadUsers -> loadUsers()
            is AccountAction.SaveProfile -> save(action.user)
            AccountAction.AckSaved -> _state.update { it.copy(profileSaved = false) }
            AccountAction.DismissError -> _state.update { it.copy(error = null) }
        }
    }

    /** Firebase keeps the session on disk; pick it up so users stay signed in across launches. */
    private fun restoreSession() {
        val uid = repository.restoredUid() ?: return
        viewModelScope.launch {
            attempt { repository.fetch(uid) }
                .onSuccess { user -> if (user != null) _state.update { it.copy(currentUser = user) } }
            // A failure here just leaves the shopper signed out; they can sign in again.
        }
    }

    private fun signIn(email: String, password: String) = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null) }
        attempt { repository.signIn(email, password) }
            .onSuccess { user -> _state.update { it.copy(loading = false, currentUser = user) } }
            .onFailure { e -> _state.update { it.copy(loading = false, error = e.toAuthMessage()) } }
    }

    private fun signUp(fullName: String, email: String, password: String) = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null) }
        // New accounts are always customers; elevated roles are granted server-side.
        val draft = User(fullName = fullName.trim(), email = email.trim(), role = UserRole.CUSTOMER)
        attempt { repository.signUp(draft, password) }
            .onSuccess { user -> _state.update { it.copy(loading = false, currentUser = user) } }
            .onFailure { e -> _state.update { it.copy(loading = false, error = e.toAuthMessage()) } }
    }

    private fun signOut() {
        repository.signOut()
        _state.update { AccountUiState() }
    }

    private fun loadProfile(userId: String) = viewModelScope.launch {
        _state.update { it.copy(loading = true, profile = null, error = null) }
        attempt { repository.fetch(userId) }
            .onSuccess { user -> _state.update { it.copy(loading = false, profile = user) } }
            .onFailure { e -> _state.update { it.copy(loading = false, error = e.toAuthMessage()) } }
    }

    private fun loadUsers() = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null) }
        attempt { repository.fetchAll() }
            .onSuccess { list -> _state.update { it.copy(loading = false, users = list) } }
            .onFailure { e -> _state.update { it.copy(loading = false, error = e.toAuthMessage()) } }
    }

    private fun save(user: User) = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null) }
        attempt { repository.update(user) }
            .onSuccess {
                _state.update { current ->
                    current.copy(
                        loading = false,
                        profile = user,
                        profileSaved = true,
                        // Editing your own profile should be reflected app-wide straight away.
                        currentUser = if (current.currentUser?.userId == user.userId) user else current.currentUser,
                        users = current.users.map { if (it.userId == user.userId) user else it }
                    )
                }
            }
            .onFailure { e -> _state.update { it.copy(loading = false, error = e.toAuthMessage()) } }
    }
}
