package mtg.app.feature.auth.infrastructure

import mtg.app.feature.auth.domain.AuthUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryAuthSessionStore {
    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    private val _isInitialized = MutableStateFlow(false)
    val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    fun updateUser(user: AuthUser?) {
        _currentUser.value = user
    }

    fun markInitialized() {
        _isInitialized.value = true
    }
}
