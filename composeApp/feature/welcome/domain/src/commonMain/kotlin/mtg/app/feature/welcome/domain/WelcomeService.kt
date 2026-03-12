package mtg.app.feature.welcome.domain

import mtg.app.core.domain.obj.AuthContext

interface WelcomeService {
    suspend fun loadNickname(uid: String): String?
    suspend fun saveNickname(context: AuthContext, nickname: String)
    suspend fun loadOnboardingCompleted(context: AuthContext): Boolean
    suspend fun saveOnboardingCompleted(context: AuthContext, completed: Boolean)
}
