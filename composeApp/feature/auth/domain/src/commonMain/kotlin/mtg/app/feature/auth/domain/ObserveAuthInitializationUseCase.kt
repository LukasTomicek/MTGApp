package mtg.app.feature.auth.domain

import kotlinx.coroutines.flow.StateFlow

class ObserveAuthInitializationUseCase(
    private val repository: AuthRepository,
) {
    operator fun invoke(): StateFlow<Boolean> = repository.isInitialized
}
