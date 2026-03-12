package mtg.app.feature.notifications.domain

import mtg.app.core.domain.obj.AuthContext

class DefaultNotificationsService(
    private val repository: NotificationsRepository,
) : NotificationsService {

    override suspend fun loadNotifications(context: AuthContext): List<NotificationItem> {
        return repository.loadNotifications(context = context)
    }

    override suspend fun markNotificationRead(context: AuthContext, notificationId: String) {
        repository.markNotificationRead(context = context, notificationId = notificationId)
    }

    override suspend fun deleteNotification(context: AuthContext, notificationId: String) {
        repository.deleteNotification(context = context, notificationId = notificationId)
    }

    override suspend fun hasUnreadNotifications(context: AuthContext): Boolean {
        return repository.hasUnreadNotifications(context = context)
    }
}
