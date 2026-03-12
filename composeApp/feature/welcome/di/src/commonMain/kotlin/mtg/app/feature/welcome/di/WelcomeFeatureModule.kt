package mtg.app.feature.welcome.di

import mtg.app.feature.welcome.data.WelcomeDataSource
import mtg.app.feature.welcome.data.remote.DefaultRemoteWelcomeDataSource
import mtg.app.feature.welcome.domain.DefaultWelcomeService
import mtg.app.feature.welcome.domain.WelcomeRepository
import mtg.app.feature.welcome.domain.WelcomeService
import mtg.app.feature.welcome.infrastructure.DefaultWelcomeRepository
import mtg.app.feature.welcome.presentation.mapguide.MapGuideViewModel
import mtg.app.feature.welcome.presentation.setupprofile.SetupProfileViewModel
import mtg.app.feature.welcome.presentation.tradeguide.TradeGuideViewModel
import mtg.app.feature.welcome.presentation.welcome.WelcomeViewModel
import org.koin.dsl.module

val welcomeFeatureModule = module {
    factory<WelcomeDataSource> { DefaultRemoteWelcomeDataSource(apiCallHandler = get()) }
    factory<WelcomeRepository> { DefaultWelcomeRepository(dataSource = get()) }
    factory<WelcomeService> { DefaultWelcomeService(repository = get()) }
    factory { WelcomeViewModel() }
    factory {
        SetupProfileViewModel(
            authService = get(),
            welcomeService = get(),
        )
    }
    factory { MapGuideViewModel(authService = get(), tradeService = get()) }
    factory { TradeGuideViewModel() }
}
