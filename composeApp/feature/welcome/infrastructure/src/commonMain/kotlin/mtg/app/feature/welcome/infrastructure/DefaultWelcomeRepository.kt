package mtg.app.feature.welcome.infrastructure

import mtg.app.feature.welcome.data.WelcomeDataSource
import mtg.app.feature.welcome.domain.WelcomeRepository
import mtg.app.core.domain.obj.AuthContext

class DefaultWelcomeRepository(
    private val dataSource: WelcomeDataSource,
) : WelcomeRepository {

    override suspend fun loadNickname(uid: String): String? {
        return dataSource.loadNickname(uid = uid)
    }

    override suspend fun saveNickname(context: AuthContext, nickname: String) {
        dataSource.saveNickname(context = context, nickname = nickname)
    }

    override suspend fun loadOnboardingCompleted(context: AuthContext): Boolean {
        return dataSource.loadOnboardingCompleted(context = context)
    }

    override suspend fun saveOnboardingCompleted(
        context: AuthContext,
        completed: Boolean,
    ) {
        dataSource.saveOnboardingCompleted(context = context, completed = completed)
    }
}
