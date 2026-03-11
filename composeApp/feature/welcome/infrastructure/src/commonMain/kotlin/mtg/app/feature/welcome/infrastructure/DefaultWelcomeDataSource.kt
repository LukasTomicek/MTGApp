package mtg.app.feature.welcome.infrastructure

import mtg.app.feature.welcome.data.WelcomeDataSource
import mtg.app.feature.welcome.infrastructure.service.WelcomeService

class DefaultWelcomeDataSource(
    private val service: WelcomeService,
) : WelcomeDataSource {

    override suspend fun loadNickname(uid: String, idToken: String): String? {
        return service.loadNickname(uid = uid, idToken = idToken)
    }

    override suspend fun saveNickname(uid: String, idToken: String, nickname: String) {
        service.saveNickname(uid = uid, idToken = idToken, nickname = nickname)
    }

    override suspend fun loadOnboardingCompleted(uid: String, idToken: String): Boolean {
        return service.loadOnboardingCompleted(uid = uid, idToken = idToken)
    }

    override suspend fun saveOnboardingCompleted(uid: String, idToken: String, completed: Boolean) {
        service.saveOnboardingCompleted(uid = uid, idToken = idToken, completed = completed)
    }
}
