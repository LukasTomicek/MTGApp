package mtg.app.feature.notifications.data

import mtg.app.feature.notifications.domain.NotificationItem
import mtg.app.core.domain.obj.AuthContext

interface NotificationsDataSource {
    suspend fun loadNotifications(context: AuthContext): List<NotificationItem>
    suspend fun markNotificationRead(context: AuthContext, notificationId: String)
    suspend fun deleteNotification(context: AuthContext, notificationId: String)
    suspend fun hasUnreadNotifications(context: AuthContext): Boolean
}
