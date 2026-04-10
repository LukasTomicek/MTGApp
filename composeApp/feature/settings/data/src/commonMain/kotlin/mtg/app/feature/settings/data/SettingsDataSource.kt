package mtg.app.feature.settings.data

import mtg.app.core.domain.obj.AuthContext
import mtg.app.feature.settings.domain.obj.SettingsProfile

interface SettingsDataSource {
    suspend fun loadOwnProfile(context: AuthContext): SettingsProfile
}
