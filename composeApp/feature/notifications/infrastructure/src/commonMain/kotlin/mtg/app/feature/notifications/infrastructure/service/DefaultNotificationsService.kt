package mtg.app.feature.notifications.infrastructure.service

import mtg.app.core.domain.config.BackendEnvironment
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.delete
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodeURLPath
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class DefaultNotificationsService(
    private val httpClient: HttpClient,
) : NotificationsService {
    private val tradeBackendBaseUrl = BackendEnvironment.primaryBaseUrl

    override suspend fun listNotifications(uid: String, idToken: String): JsonObject {
        val user = uid.encodeURLPath()
        println("TradeBE: calling /v1/bridge/users/$uid/notifications")
        val response = httpClient.get("$tradeBackendBaseUrl/v1/bridge/users/$user/notifications")
        val json = response.bodyAsText().trim()
        return if (json == "null" || json.isBlank()) JsonObject(emptyMap()) else {
            Json.parseToJsonElement(json) as? JsonObject ?: JsonObject(emptyMap())
        }
    }

    override suspend fun markNotificationRead(uid: String, idToken: String, notificationId: String) {
        val user = uid.encodeURLPath()
        val id = notificationId.encodeURLPath()
        println("TradeBE: calling /v1/bridge/users/$uid/notifications/$notificationId PATCH")
        httpClient.patch("$tradeBackendBaseUrl/v1/bridge/users/$user/notifications/$id") {
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonObject {
                    put("isRead", true)
                }
            )
        }
    }

    override suspend fun deleteNotification(uid: String, idToken: String, notificationId: String) {
        val user = uid.encodeURLPath()
        val id = notificationId.encodeURLPath()
        println("TradeBE: calling /v1/bridge/users/$uid/notifications/$notificationId DELETE")
        httpClient.delete("$tradeBackendBaseUrl/v1/bridge/users/$user/notifications/$id")
    }

    override suspend fun hasUnreadNotifications(uid: String, idToken: String): Boolean {
        val user = uid.encodeURLPath()
        println("TradeBE: calling /v1/bridge/users/$uid/notifications/unread")
        val response = httpClient.get("$tradeBackendBaseUrl/v1/bridge/users/$user/notifications/unread")
        val json = response.bodyAsText().trim()
        val root = Json.parseToJsonElement(json) as? JsonObject ?: return false
        val hasUnreadRaw = (root["hasUnread"] as? JsonPrimitive)?.booleanOrNull ?: false
        return hasUnreadRaw
    }
}
