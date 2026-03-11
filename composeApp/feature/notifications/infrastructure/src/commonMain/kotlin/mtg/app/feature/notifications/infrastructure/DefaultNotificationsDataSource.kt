package mtg.app.feature.notifications.infrastructure

import mtg.app.feature.notifications.data.NotificationsDataSource
import mtg.app.feature.notifications.data.toNotificationItems
import mtg.app.feature.notifications.domain.NotificationItem
import mtg.app.feature.notifications.infrastructure.service.NotificationsService

class DefaultNotificationsDataSource(
    private val service: NotificationsService,
) : NotificationsDataSource {
    override suspend fun loadNotifications(uid: String, idToken: String): List<NotificationItem> {
        return service.listNotifications(uid = uid, idToken = idToken).toNotificationItems()
    }

    override suspend fun markNotificationRead(uid: String, idToken: String, notificationId: String) {
        service.markNotificationRead(uid = uid, idToken = idToken, notificationId = notificationId)
    }

    override suspend fun deleteNotification(uid: String, idToken: String, notificationId: String) {
        service.deleteNotification(uid = uid, idToken = idToken, notificationId = notificationId)
    }

    override suspend fun hasUnreadNotifications(uid: String, idToken: String): Boolean {
        return service.hasUnreadNotifications(uid = uid, idToken = idToken)
    }
}
