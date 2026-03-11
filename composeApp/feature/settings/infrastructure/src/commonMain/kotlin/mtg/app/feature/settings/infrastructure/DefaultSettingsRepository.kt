package mtg.app.feature.settings.infrastructure

import mtg.app.feature.settings.data.SettingsDataSource
import mtg.app.feature.settings.domain.SettingsRepository

class DefaultSettingsRepository(
    private val dataSource: SettingsDataSource,
) : SettingsRepository {

}
