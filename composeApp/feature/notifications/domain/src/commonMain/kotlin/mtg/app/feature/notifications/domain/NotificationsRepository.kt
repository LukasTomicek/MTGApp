package mtg.app.feature.notifications.domain

import mtg.app.core.domain.obj.AuthContext

interface NotificationsRepository {
    suspend fun loadNotifications(context: AuthContext): List<NotificationItem>
    suspend fun markNotificationRead(context: AuthContext, notificationId: String)
    suspend fun deleteNotification(context: AuthContext, notificationId: String)
    suspend fun hasUnreadNotifications(context: AuthContext): Boolean
}
