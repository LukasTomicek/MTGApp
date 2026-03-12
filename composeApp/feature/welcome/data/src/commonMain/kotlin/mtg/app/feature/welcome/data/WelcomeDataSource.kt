package mtg.app.feature.welcome.data

import mtg.app.core.domain.obj.AuthContext

interface WelcomeDataSource {
    suspend fun loadNickname(uid: String): String?
    suspend fun saveNickname(context: AuthContext, nickname: String)
    suspend fun loadOnboardingCompleted(context: AuthContext): Boolean
    suspend fun saveOnboardingCompleted(context: AuthContext, completed: Boolean)
}
