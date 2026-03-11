package mtg.app.feature.settings.di

import mtg.app.feature.settings.data.SettingsDataSource
import mtg.app.feature.settings.domain.GetSettingsMessageUseCase
import mtg.app.feature.settings.domain.SettingsRepository
import mtg.app.feature.settings.infrastructure.DefaultSettingsDataSource
import mtg.app.feature.settings.infrastructure.DefaultSettingsRepository
import mtg.app.feature.settings.infrastructure.service.DefaultSettingsService
import mtg.app.feature.settings.infrastructure.service.SettingsService
import mtg.app.feature.settings.presentation.SettingsViewModel
import mtg.app.feature.settings.presentation.profile.ProfileViewModel
import org.koin.dsl.module

val settingsFeatureModule = module {
    factory<SettingsService> { DefaultSettingsService() }
    factory<SettingsDataSource> { DefaultSettingsDataSource(service = get()) }
    factory<SettingsRepository> { DefaultSettingsRepository(dataSource = get()) }
    factory { GetSettingsMessageUseCase(repository = get()) }
    factory { SettingsViewModel(getMessage = get(), signOut = get(), deleteAccountUseCase = get()) }
    factory {
        ProfileViewModel(
            observeAuthState = get(),
            loadWelcomeNickname = get(),
            saveWelcomeNickname = get(),
            changePasswordUseCase = get(),
            loadUserReviews = get(),
        )
    }
}
