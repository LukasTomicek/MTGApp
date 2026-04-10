package mtg.app.feature.settings.infrastructure

import mtg.app.core.domain.obj.AuthContext
import mtg.app.feature.settings.data.SettingsDataSource
import mtg.app.feature.settings.domain.SettingsRepository
import mtg.app.feature.settings.domain.obj.SettingsProfile

class DefaultSettingsRepository(
    private val dataSource: SettingsDataSource,
) : SettingsRepository {
    override suspend fun loadOwnProfile(context: AuthContext): SettingsProfile {
        return dataSource.loadOwnProfile(context = context)
    }
}
