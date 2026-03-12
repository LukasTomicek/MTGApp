package mtg.app.feature.notifications.infrastructure

import mtg.app.feature.notifications.data.NotificationsDataSource
import mtg.app.feature.notifications.domain.NotificationsRepository
import mtg.app.core.domain.obj.AuthContext

class DefaultNotificationsRepository(
    private val dataSource: NotificationsDataSource,
) : NotificationsRepository {
    override suspend fun loadNotifications(context: AuthContext) =
        dataSource.loadNotifications(context = context)

    override suspend fun markNotificationRead(context: AuthContext, notificationId: String) {
        dataSource.markNotificationRead(context = context, notificationId = notificationId)
    }

    override suspend fun deleteNotification(context: AuthContext, notificationId: String) {
        dataSource.deleteNotification(context = context, notificationId = notificationId)
    }

    override suspend fun hasUnreadNotifications(context: AuthContext): Boolean {
        return dataSource.hasUnreadNotifications(context = context)
    }
}
