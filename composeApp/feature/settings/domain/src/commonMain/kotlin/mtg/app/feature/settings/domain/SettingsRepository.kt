package mtg.app.feature.settings.domain

import mtg.app.core.domain.obj.AuthContext
import mtg.app.feature.settings.domain.obj.SettingsProfile

interface SettingsRepository {
    suspend fun loadOwnProfile(context: AuthContext): SettingsProfile
}
