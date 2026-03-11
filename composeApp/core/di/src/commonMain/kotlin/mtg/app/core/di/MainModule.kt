package mtg.app.core.di

import mtg.app.core.presentation.MainViewModel
import org.koin.dsl.module

val mainModule = module {
    factory { MainViewModel() }
}
