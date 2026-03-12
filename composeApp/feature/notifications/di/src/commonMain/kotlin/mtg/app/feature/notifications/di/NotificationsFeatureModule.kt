package mtg.app.feature.notifications.di

import mtg.app.feature.notifications.data.NotificationsDataSource
import mtg.app.feature.notifications.data.remote.DefaultRemoteNotificationsDataSource
import mtg.app.feature.notifications.domain.DefaultNotificationsService
import mtg.app.feature.notifications.domain.NotificationsRepository
import mtg.app.feature.notifications.domain.NotificationsService
import mtg.app.feature.notifications.infrastructure.DefaultNotificationsRepository
import mtg.app.feature.notifications.presentation.NotificationsBadgeViewModel
import mtg.app.feature.notifications.presentation.NotificationsViewModel
import org.koin.dsl.module

val notificationsFeatureModule = module {
    factory<NotificationsDataSource> { DefaultRemoteNotificationsDataSource(apiCallHandler = get()) }
    factory<NotificationsRepository> { DefaultNotificationsRepository(dataSource = get()) }
    factory<NotificationsService> { DefaultNotificationsService(repository = get()) }
    factory {
        NotificationsViewModel(
            authService = get(),
            notificationsService = get(),
        )
    }
    factory {
        NotificationsBadgeViewModel(
            authService = get(),
            notificationsService = get(),
        )
    }
}
