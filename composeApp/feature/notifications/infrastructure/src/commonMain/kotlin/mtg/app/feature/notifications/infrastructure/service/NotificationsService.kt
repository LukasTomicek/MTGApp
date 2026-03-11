package mtg.app.feature.notifications.infrastructure.service

import kotlinx.serialization.json.JsonObject

interface NotificationsService {
    suspend fun listNotifications(uid: String, idToken: String): JsonObject
    suspend fun markNotificationRead(uid: String, idToken: String, notificationId: String)
    suspend fun deleteNotification(uid: String, idToken: String, notificationId: String)
    suspend fun hasUnreadNotifications(uid: String, idToken: String): Boolean
}
