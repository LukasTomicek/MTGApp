package mtg.app.feature.welcome.domain

interface WelcomeRepository {
    suspend fun loadNickname(uid: String, idToken: String): String?
    suspend fun saveNickname(uid: String, idToken: String, nickname: String)
    suspend fun loadOnboardingCompleted(uid: String, idToken: String): Boolean
    suspend fun saveOnboardingCompleted(uid: String, idToken: String, completed: Boolean)
}
