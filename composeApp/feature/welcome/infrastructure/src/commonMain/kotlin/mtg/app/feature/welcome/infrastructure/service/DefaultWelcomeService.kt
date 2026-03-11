package mtg.app.feature.welcome.infrastructure.service

import mtg.app.core.domain.config.BackendEnvironment
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodeURLPath
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class DefaultWelcomeService(
    private val httpClient: HttpClient,
) : WelcomeService {
    private val tradeBackendBaseUrls = listOf(BackendEnvironment.primaryBaseUrl) + BackendEnvironment.fallbackBaseUrls

    override suspend fun loadNickname(uid: String, idToken: String): String? {
        val response = getWithBaseFallback(
            endpointLabel = "/v1/users/profile",
            uid = uid,
        ) { baseUrl ->
            httpClient.get("$baseUrl/v1/users/profile") {
                parameter("userId", uid)
            }
        }
        if (!response.status.isSuccess()) return null

        val root = parseJsonObject(response.bodyAsText())
        return (root["nickname"] as? JsonPrimitive)
            ?.content
            ?.trim()
            ?.takeUnless { it.isBlank() }
    }

    override suspend fun saveNickname(uid: String, idToken: String, nickname: String) {
        val trimmedNickname = nickname.trim()
        if (trimmedNickname.isBlank()) {
            throw IllegalArgumentException("Nickname is required")
        }

        val response = putWithBaseFallback(
            endpointLabel = "/v1/users/profile",
            uid = uid,
        ) { baseUrl ->
            httpClient.put("$baseUrl/v1/users/profile") {
                contentType(ContentType.Application.Json)
                setBody(
                    buildJsonObject {
                        put("userId", uid)
                        put("nickname", trimmedNickname)
                    }
                )
            }
        }

        if (!response.status.isSuccess()) {
            val message = parseBackendError(response.bodyAsText())
            throw IllegalStateException(message)
        }
    }

    override suspend fun loadOnboardingCompleted(uid: String, idToken: String): Boolean {
        val user = uid.encodeURLPath()
        val response = getWithBaseFallback(
            endpointLabel = "/v1/bridge/users/$uid/profile/onboarding",
            uid = uid,
        ) { baseUrl ->
            httpClient.get("$baseUrl/v1/bridge/users/$user/profile/onboarding")
        }
        if (!response.status.isSuccess()) return false

        val root = parseJsonObject(response.bodyAsText())
        return (root["completed"] as? JsonPrimitive)?.content?.toBooleanStrictOrNull() ?: false
    }

    override suspend fun saveOnboardingCompleted(uid: String, idToken: String, completed: Boolean) {
        val user = uid.encodeURLPath()
        val response = putWithBaseFallback(
            endpointLabel = "/v1/bridge/users/$uid/profile/onboarding",
            uid = uid,
        ) { baseUrl ->
            httpClient.put("$baseUrl/v1/bridge/users/$user/profile/onboarding") {
                contentType(ContentType.Application.Json)
                setBody(
                    buildJsonObject {
                        put("completed", completed)
                    }
                )
            }
        }

        if (!response.status.isSuccess()) {
            val message = parseBackendError(response.bodyAsText())
            throw IllegalStateException(message)
        }
    }

    private fun parseJsonObject(raw: String): JsonObject {
        val body = raw.trim()
        if (body.isBlank() || body == "null") return JsonObject(emptyMap())
        return runCatching { Json.parseToJsonElement(body) as? JsonObject }
            .getOrNull()
            ?: JsonObject(emptyMap())
    }

    private fun parseBackendError(raw: String): String {
        val root = parseJsonObject(raw)
        val message = (root["message"] as? JsonPrimitive)?.content?.trim().orEmpty()
        return message.ifBlank { "Request failed" }
    }

    private suspend fun getWithBaseFallback(
        endpointLabel: String,
        uid: String,
        callFactory: suspend (String) -> io.ktor.client.statement.HttpResponse,
    ): io.ktor.client.statement.HttpResponse {
        var lastError: Throwable? = null
        tradeBackendBaseUrls.forEachIndexed { index, baseUrl ->
            try {
                println("TradeBE: calling $endpointLabel for uid=$uid base=$baseUrl")
                return callFactory(baseUrl)
            } catch (t: Throwable) {
                lastError = t
                val hasNext = index < tradeBackendBaseUrls.lastIndex
                println(
                    "TradeBE: $endpointLabel failed for uid=$uid base=$baseUrl retryNext=$hasNext error=${t.message.orEmpty()}"
                )
            }
        }
        throw lastError ?: IllegalStateException("Backend unavailable")
    }

    private suspend fun putWithBaseFallback(
        endpointLabel: String,
        uid: String,
        callFactory: suspend (String) -> io.ktor.client.statement.HttpResponse,
    ): io.ktor.client.statement.HttpResponse {
        var lastError: Throwable? = null
        tradeBackendBaseUrls.forEachIndexed { index, baseUrl ->
            try {
                println("TradeBE: calling PUT $endpointLabel for uid=$uid base=$baseUrl")
                return callFactory(baseUrl)
            } catch (t: Throwable) {
                lastError = t
                val hasNext = index < tradeBackendBaseUrls.lastIndex
                println(
                    "TradeBE: PUT $endpointLabel failed for uid=$uid base=$baseUrl retryNext=$hasNext error=${t.message.orEmpty()}"
                )
            }
        }
        throw lastError ?: IllegalStateException("Backend unavailable")
    }
}
