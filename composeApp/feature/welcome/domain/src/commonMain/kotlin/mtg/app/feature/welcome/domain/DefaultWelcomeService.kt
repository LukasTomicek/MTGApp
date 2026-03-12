package mtg.app.feature.welcome.domain

import mtg.app.core.domain.obj.AuthContext

class DefaultWelcomeService(
    private val repository: WelcomeRepository,
) : WelcomeService {

    override suspend fun loadNickname(uid: String): String? {
        return repository.loadNickname(uid = uid)
    }

    override suspend fun saveNickname(context: AuthContext, nickname: String) {
        repository.saveNickname(context = context, nickname = nickname)
    }

    override suspend fun loadOnboardingCompleted(context: AuthContext): Boolean {
        return repository.loadOnboardingCompleted(context = context)
    }

    override suspend fun saveOnboardingCompleted(
        context: AuthContext,
        completed: Boolean,
    ) {
        repository.saveOnboardingCompleted(context = context, completed = completed)
    }
}
