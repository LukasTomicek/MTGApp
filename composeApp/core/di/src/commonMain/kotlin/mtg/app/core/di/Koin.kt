package mtg.app.core.di

import mtg.app.core.data.remote.coreDataModule
import mtg.app.feature.auth.di.authFeatureModule
import mtg.app.feature.chat.di.chatFeatureModule
import mtg.app.feature.map.di.mapFeatureModule
import mtg.app.feature.notifications.di.notificationsFeatureModule
import mtg.app.feature.settings.data.SettingsDataSource
import mtg.app.feature.settings.data.remote.DefaultRemoteSettingsDataSource
import mtg.app.feature.settings.domain.DefaultSettingsService
import mtg.app.feature.settings.domain.SettingsRepository
import mtg.app.feature.settings.domain.SettingsService
import mtg.app.feature.settings.infrastructure.DefaultSettingsRepository
import mtg.app.feature.settings.presentation.SettingsViewModel
import mtg.app.feature.settings.presentation.profile.ProfileViewModel
import mtg.app.feature.trade.di.tradeFeatureModule
import mtg.app.feature.welcome.di.welcomeFeatureModule
import org.koin.core.context.startKoin
import org.koin.dsl.module

private var koinStarted: Boolean = false

private val settingsFeatureModule = module {
    factory<SettingsDataSource> { DefaultRemoteSettingsDataSource(apiCallHandler = get()) }
    factory<SettingsRepository> { DefaultSettingsRepository(dataSource = get()) }
    factory<SettingsService> { DefaultSettingsService(repository = get()) }
    factory { SettingsViewModel(authService = get()) }
    factory {
        ProfileViewModel(
            authService = get(),
            welcomeService = get(),
            chatService = get(),
            settingsService = get(),
        )
    }
}

fun initKoin() {
    if (koinStarted) {
        return
    }

    startKoin {
        modules(
            mainModule,
            coreDataModule,
            authFeatureModule,
            welcomeFeatureModule,
            tradeFeatureModule,
            mapFeatureModule,
            settingsFeatureModule,
            chatFeatureModule,
            notificationsFeatureModule,
        )
    }
    koinStarted = true
}
