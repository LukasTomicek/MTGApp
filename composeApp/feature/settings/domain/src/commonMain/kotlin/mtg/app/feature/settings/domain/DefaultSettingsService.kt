package mtg.app.feature.settings.domain

import mtg.app.core.domain.obj.AuthContext
import mtg.app.feature.settings.domain.obj.SettingsProfile

class DefaultSettingsService(
    private val repository: SettingsRepository,
) : SettingsService {
    override suspend fun loadOwnProfile(context: AuthContext): SettingsProfile {
        return repository.loadOwnProfile(context = context)
    }
}
