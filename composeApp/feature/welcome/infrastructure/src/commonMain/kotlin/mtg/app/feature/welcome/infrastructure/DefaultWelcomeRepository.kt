package mtg.app.feature.welcome.infrastructure

import mtg.app.feature.welcome.data.WelcomeDataSource
import mtg.app.feature.welcome.domain.WelcomeRepository

class DefaultWelcomeRepository(
    private val dataSource: WelcomeDataSource,
) : WelcomeRepository {

    override suspend fun loadNickname(uid: String, idToken: String): String? {
        return dataSource.loadNickname(uid = uid, idToken = idToken)
    }

    override suspend fun saveNickname(uid: String, idToken: String, nickname: String) {
        dataSource.saveNickname(uid = uid, idToken = idToken, nickname = nickname)
    }

    override suspend fun loadOnboardingCompleted(uid: String, idToken: String): Boolean {
        return dataSource.loadOnboardingCompleted(uid = uid, idToken = idToken)
    }

    override suspend fun saveOnboardingCompleted(uid: String, idToken: String, completed: Boolean) {
        dataSource.saveOnboardingCompleted(uid = uid, idToken = idToken, completed = completed)
    }
}
