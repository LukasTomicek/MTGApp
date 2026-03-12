package mtg.app.feature.notifications.infrastructure.service

import mtg.app.core.domain.config.BackendEnvironment
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.encodeURLPath
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull

class DefaultNotificationsService(
    private val httpClient: HttpClient,
) : NotificationsService {
    private val tradeBackendBaseUrl = BackendEnvironment.primaryBaseUrl

    override suspend fun listNotifications(uid: String, idToken: String): JsonObject {
        println("TradeBE: calling /v1/users/me/notifications")
        val response = httpClient.get("$tradeBackendBaseUrl/v1/users/me/notifications") {
            withBearer(idToken)
        }
        val json = response.bodyAsText().trim()
        return if (json == "null" || json.isBlank()) JsonObject(emptyMap()) else {
            Json.parseToJsonElement(json) as? JsonObject ?: JsonObject(emptyMap())
        }
    }

    override suspend fun markNotificationRead(uid: String, idToken: String, notificationId: String) {
        val id = notificationId.encodeURLPath()
        println("TradeBE: calling /v1/users/me/notifications/$notificationId/read PATCH")
        httpClient.patch("$tradeBackendBaseUrl/v1/users/me/notifications/$id/read") {
            withBearer(idToken)
        }
    }

    override suspend fun deleteNotification(uid: String, idToken: String, notificationId: String) {
        val id = notificationId.encodeURLPath()
        println("TradeBE: calling /v1/users/me/notifications/$notificationId DELETE")
        httpClient.delete("$tradeBackendBaseUrl/v1/users/me/notifications/$id") {
            withBearer(idToken)
        }
    }

    override suspend fun hasUnreadNotifications(uid: String, idToken: String): Boolean {
        println("TradeBE: calling /v1/users/me/notifications/unread")
        val response = httpClient.get("$tradeBackendBaseUrl/v1/users/me/notifications/unread") {
            withBearer(idToken)
        }
        val json = response.bodyAsText().trim()
        val root = Json.parseToJsonElement(json) as? JsonObject ?: return false
        val hasUnreadRaw = (root["hasUnread"] as? JsonPrimitive)?.booleanOrNull ?: false
        return hasUnreadRaw
    }
}

private fun HttpRequestBuilder.withBearer(idToken: String) {
    header(HttpHeaders.Authorization, "Bearer $idToken")
}
