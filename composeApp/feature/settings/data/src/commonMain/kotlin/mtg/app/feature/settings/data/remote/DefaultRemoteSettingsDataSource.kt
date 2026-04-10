package mtg.app.feature.settings.data.remote

import mtg.app.core.data.remote.ApiCallHandler
import mtg.app.core.domain.obj.AuthContext
import mtg.app.feature.settings.data.SettingsDataSource
import mtg.app.feature.settings.data.remote.dto.OwnProfileResponseDto
import mtg.app.feature.settings.domain.obj.SettingsProfile

class DefaultRemoteSettingsDataSource(
    private val apiCallHandler: ApiCallHandler,
) : SettingsDataSource {
    override suspend fun loadOwnProfile(context: AuthContext): SettingsProfile {
        val response = apiCallHandler.apiRequest<OwnProfileResponseDto>(
            path = "/v1/users/me/profile",
            idToken = context.idToken,
        )
        return SettingsProfile(
            nickname = response.nickname?.trim().orEmpty(),
            balanceMinor = response.balanceMinor,
        )
    }
}
