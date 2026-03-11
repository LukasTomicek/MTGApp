package mtg.app.feature.notifications.di

import mtg.app.feature.notifications.data.NotificationsDataSource
import mtg.app.feature.notifications.domain.DeleteNotificationUseCase
import mtg.app.feature.notifications.domain.HasUnreadNotificationsUseCase
import mtg.app.feature.notifications.domain.LoadNotificationsUseCase
import mtg.app.feature.notifications.domain.MarkNotificationReadUseCase
import mtg.app.feature.notifications.domain.NotificationsRepository
import mtg.app.feature.notifications.infrastructure.DefaultNotificationsDataSource
import mtg.app.feature.notifications.infrastructure.DefaultNotificationsRepository
import mtg.app.feature.notifications.infrastructure.service.DefaultNotificationsService
import mtg.app.feature.notifications.infrastructure.service.NotificationsService
import mtg.app.feature.notifications.presentation.NotificationsBadgeViewModel
import mtg.app.feature.notifications.presentation.NotificationsViewModel
import org.koin.dsl.module

val notificationsFeatureModule = module {
    factory<NotificationsService> { DefaultNotificationsService(httpClient = get()) }
    factory<NotificationsDataSource> { DefaultNotificationsDataSource(service = get()) }
    factory<NotificationsRepository> { DefaultNotificationsRepository(dataSource = get()) }
    factory { LoadNotificationsUseCase(repository = get()) }
    factory { MarkNotificationReadUseCase(repository = get()) }
    factory { DeleteNotificationUseCase(repository = get()) }
    factory { HasUnreadNotificationsUseCase(repository = get()) }
    factory {
        NotificationsViewModel(
            observeAuthState = get(),
            loadNotifications = get(),
            markNotificationRead = get(),
            deleteNotification = get(),
        )
    }
    factory {
        NotificationsBadgeViewModel(
            observeAuthState = get(),
            hasUnreadNotifications = get(),
        )
    }
}
