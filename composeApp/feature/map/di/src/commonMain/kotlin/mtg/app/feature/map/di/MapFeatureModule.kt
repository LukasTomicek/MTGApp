package mtg.app.feature.map.di

import mtg.app.feature.map.presentation.map.MapViewModel
import org.koin.dsl.module

val mapFeatureModule = module {
    factory {
        MapViewModel(
            observeAuthState = get(),
            loadMapPins = get(),
            replaceMapPins = get(),
        )
    }
}
