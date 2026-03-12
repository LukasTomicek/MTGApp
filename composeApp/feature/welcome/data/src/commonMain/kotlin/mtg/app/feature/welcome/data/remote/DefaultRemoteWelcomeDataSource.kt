package mtg.app.feature.welcome.data.remote

import io.ktor.http.HttpMethod
import mtg.app.core.data.remote.ApiCallHandler
import mtg.app.core.domain.obj.AuthContext
import mtg.app.feature.welcome.data.WelcomeDataSource
import mtg.app.feature.welcome.data.remote.dto.NicknameResponseDto
import mtg.app.feature.welcome.data.remote.dto.OnboardingCompletedResponseDto
import mtg.app.feature.welcome.data.remote.dto.UpdateNicknameRequestDto
import mtg.app.feature.welcome.data.remote.dto.UpdateOnboardingRequestDto

class DefaultRemoteWelcomeDataSource(
    private val apiCallHandler: ApiCallHandler,
) : WelcomeDataSource {
    override suspend fun loadNickname(uid: String): String? {
        val response = apiCallHandler.apiRequest<NicknameResponseDto>(
            path = "/v1/users/profile/$uid",
            requiresAuth = false,
        )
        return response.nickname
            ?.trim()
            ?.takeUnless { it.isBlank() }
    }

    override suspend fun saveNickname(context: AuthContext, nickname: String) {
        val trimmedNickname = nickname.trim()
        require(trimmedNickname.isNotBlank()) { "Nickname is required" }

        apiCallHandler.apiRequest<Unit>(
            path = "/v1/users/me/profile",
            method = HttpMethod.Put,
            idToken = context.idToken,
            body = UpdateNicknameRequestDto(nickname = trimmedNickname),
        )
    }

    override suspend fun loadOnboardingCompleted(context: AuthContext): Boolean {
        val response = apiCallHandler.apiRequest<OnboardingCompletedResponseDto>(
            path = "/v1/users/me/onboarding",
            idToken = context.idToken,
        )
        return response.completed
    }

    override suspend fun saveOnboardingCompleted(
        context: AuthContext,
        completed: Boolean,
    ) {
        apiCallHandler.apiRequest<Unit>(
            path = "/v1/users/me/onboarding",
            method = HttpMethod.Put,
            idToken = context.idToken,
            body = UpdateOnboardingRequestDto(completed = completed),
        )
    }
}
