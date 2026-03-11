package mtg.app.feature.notifications.domain

interface NotificationsRepository {
    suspend fun loadNotifications(uid: String, idToken: String): List<NotificationItem>
    suspend fun markNotificationRead(uid: String, idToken: String, notificationId: String)
    suspend fun deleteNotification(uid: String, idToken: String, notificationId: String)
    suspend fun hasUnreadNotifications(uid: String, idToken: String): Boolean
}
