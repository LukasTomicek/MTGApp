package mtg.app.core.di

import mtg.app.core.data.remote.coreDataModule
import mtg.app.feature.auth.di.authFeatureModule
import mtg.app.feature.chat.di.chatFeatureModule
import mtg.app.feature.map.di.mapFeatureModule
import mtg.app.feature.notifications.di.notificationsFeatureModule
import mtg.app.feature.settings.di.settingsFeatureModule
import mtg.app.feature.trade.di.tradeFeatureModule
import mtg.app.feature.welcome.di.welcomeFeatureModule
import org.koin.core.context.startKoin

private var koinStarted: Boolean = false

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
