package mtg.app.feature.settings.di

import mtg.app.feature.settings.data.SettingsDataSource
import mtg.app.feature.settings.data.remote.DefaultRemoteSettingsDataSource
import mtg.app.feature.settings.domain.SettingsRepository
import mtg.app.feature.settings.infrastructure.DefaultSettingsRepository
import mtg.app.feature.settings.presentation.SettingsViewModel
import mtg.app.feature.settings.presentation.profile.ProfileViewModel
import org.koin.dsl.module

val settingsFeatureModule = module {
    factory<SettingsDataSource> { DefaultRemoteSettingsDataSource() }
    factory<SettingsRepository> { DefaultSettingsRepository(dataSource = get()) }
    factory { SettingsViewModel(authService = get()) }
    factory {
        ProfileViewModel(
            authService = get(),
            welcomeService = get(),
            chatService = get(),
        )
    }
}
