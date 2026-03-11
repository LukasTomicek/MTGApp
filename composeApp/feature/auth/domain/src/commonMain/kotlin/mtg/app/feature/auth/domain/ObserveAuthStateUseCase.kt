package mtg.app.feature.auth.domain

import kotlinx.coroutines.flow.StateFlow

class ObserveAuthStateUseCase(
    private val repository: AuthRepository,
) {
    operator fun invoke(): StateFlow<AuthUser?> = repository.currentUser
}
