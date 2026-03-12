package mtg.app.feature.map.di

import mtg.app.feature.map.presentation.map.MapViewModel
import org.koin.dsl.module

val mapFeatureModule = module {
    factory {
        MapViewModel(
            authService = get(),
            tradeService = get(),
        )
    }
}
