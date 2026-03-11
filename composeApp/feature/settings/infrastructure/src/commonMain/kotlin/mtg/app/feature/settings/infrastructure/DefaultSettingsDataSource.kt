package mtg.app.feature.settings.infrastructure

import mtg.app.feature.settings.data.SettingsDataSource
import mtg.app.feature.settings.infrastructure.service.SettingsService

class DefaultSettingsDataSource(
    private val service: SettingsService,
) : SettingsDataSource {

}
