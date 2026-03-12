package mtg.app.feature.notifications.data.remote

import mtg.app.core.data.remote.ApiCallHandler
import mtg.app.core.domain.obj.AuthContext
import mtg.app.feature.notifications.data.NotificationsDataSource
import mtg.app.feature.notifications.data.remote.dto.HasUnreadNotificationsResponseDto
import mtg.app.feature.notifications.data.remote.dto.NotificationEntryDto
import mtg.app.feature.notifications.data.remote.dto.toNotificationItems
import mtg.app.feature.notifications.domain.NotificationItem
import io.ktor.http.HttpMethod

class DefaultRemoteNotificationsDataSource(
    private val apiCallHandler: ApiCallHandler,
) : NotificationsDataSource {
    override suspend fun loadNotifications(context: AuthContext): List<NotificationItem> {
        return apiCallHandler.apiRequest<Map<String, NotificationEntryDto>>(
            path = "/v1/users/me/notifications",
            idToken = context.idToken,
        ).toNotificationItems()
    }

    override suspend fun markNotificationRead(context: AuthContext, notificationId: String) {
        apiCallHandler.apiRequest<Unit>(
            path = "/v1/users/me/notifications/${notificationId}/read",
            method = HttpMethod.Patch,
            idToken = context.idToken,
        )
    }

    override suspend fun deleteNotification(context: AuthContext, notificationId: String) {
        apiCallHandler.apiRequest<Unit>(
            path = "/v1/users/me/notifications/${notificationId}",
            method = HttpMethod.Delete,
            idToken = context.idToken,
        )
    }

    override suspend fun hasUnreadNotifications(context: AuthContext): Boolean {
        val response = apiCallHandler.apiRequest<HasUnreadNotificationsResponseDto>(
            path = "/v1/users/me/notifications/unread",
            idToken = context.idToken,
        )
        return response.hasUnread
    }
}
