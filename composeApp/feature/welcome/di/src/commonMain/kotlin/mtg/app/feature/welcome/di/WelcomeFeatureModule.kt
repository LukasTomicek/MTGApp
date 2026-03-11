package mtg.app.feature.welcome.di

import mtg.app.feature.welcome.data.WelcomeDataSource
import mtg.app.feature.welcome.domain.GetWelcomeMessageUseCase
import mtg.app.feature.welcome.domain.LoadOnboardingCompletedUseCase
import mtg.app.feature.welcome.domain.LoadWelcomeNicknameUseCase
import mtg.app.feature.welcome.domain.SaveOnboardingCompletedUseCase
import mtg.app.feature.welcome.domain.SaveWelcomeNicknameUseCase
import mtg.app.feature.welcome.domain.WelcomeRepository
import mtg.app.feature.welcome.infrastructure.DefaultWelcomeDataSource
import mtg.app.feature.welcome.infrastructure.DefaultWelcomeRepository
import mtg.app.feature.welcome.infrastructure.service.DefaultWelcomeService
import mtg.app.feature.welcome.infrastructure.service.WelcomeService
import mtg.app.feature.welcome.presentation.mapguide.MapGuideViewModel
import mtg.app.feature.welcome.presentation.setupprofile.SetupProfileViewModel
import mtg.app.feature.welcome.presentation.tradeguide.TradeGuideViewModel
import mtg.app.feature.welcome.presentation.welcome.WelcomeViewModel
import org.koin.dsl.module

val welcomeFeatureModule = module {
    factory<WelcomeService> { DefaultWelcomeService(httpClient = get()) }
    factory<WelcomeDataSource> { DefaultWelcomeDataSource(service = get()) }
    factory<WelcomeRepository> { DefaultWelcomeRepository(dataSource = get()) }
    factory { GetWelcomeMessageUseCase(repository = get()) }
    factory { LoadOnboardingCompletedUseCase(repository = get()) }
    factory { LoadWelcomeNicknameUseCase(repository = get()) }
    factory { SaveOnboardingCompletedUseCase(repository = get()) }
    factory { SaveWelcomeNicknameUseCase(repository = get()) }
    factory { WelcomeViewModel() }
    factory {
        SetupProfileViewModel(
            observeAuthState = get(),
            loadWelcomeNickname = get(),
            saveWelcomeNickname = get(),
            saveOnboardingCompleted = get(),
        )
    }
    factory { MapGuideViewModel(observeAuthState = get(), loadMapPins = get(), replaceMapPins = get()) }
    factory { TradeGuideViewModel() }
}
