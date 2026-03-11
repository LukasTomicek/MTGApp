package mtg.app.feature.notifications.infrastructure

import mtg.app.feature.notifications.data.NotificationsDataSource
import mtg.app.feature.notifications.domain.NotificationsRepository

class DefaultNotificationsRepository(
    private val dataSource: NotificationsDataSource,
) : NotificationsRepository {
    override suspend fun loadNotifications(uid: String, idToken: String) =
        dataSource.loadNotifications(uid = uid, idToken = idToken)

    override suspend fun markNotificationRead(uid: String, idToken: String, notificationId: String) {
        dataSource.markNotificationRead(uid = uid, idToken = idToken, notificationId = notificationId)
    }

    override suspend fun deleteNotification(uid: String, idToken: String, notificationId: String) {
        dataSource.deleteNotification(uid = uid, idToken = idToken, notificationId = notificationId)
    }

    override suspend fun hasUnreadNotifications(uid: String, idToken: String): Boolean {
        return dataSource.hasUnreadNotifications(uid = uid, idToken = idToken)
    }
}
