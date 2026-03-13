package mtg.app.core.data.remote

import mtg.app.core.domain.config.BackendEnvironment
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.decodeFromString

class ApiCallHandler(
    private val httpClient: HttpClient,
    @PublishedApi internal val json: Json,
    private val tokenProvider: TokenProvider,
) {
    suspend inline fun <reified T> apiRequest(
        path: String,
        method: HttpMethod = HttpMethod.Get,
        body: Any? = null,
        idToken: String? = null,
        requiresAuth: Boolean = true,
        noinline builder: HttpRequestBuilder.() -> Unit = {},
    ): T {
        val response = backendRequest(
            path = path,
            method = method,
            body = body,
            idToken = idToken,
            requiresAuth = requiresAuth,
            builder = builder,
        )
        requireSuccess(response = response, action = "$method $path")
        return parseResponse(response.bodyAsText())
    }

    suspend inline fun <reified T> apiRequestOrNull(
        path: String,
        method: HttpMethod = HttpMethod.Get,
        body: Any? = null,
        idToken: String? = null,
        requiresAuth: Boolean = true,
        nullStatusCode: Int = 404,
        noinline builder: HttpRequestBuilder.() -> Unit = {},
    ): T? {
        val response = backendRequest(
            path = path,
            method = method,
            body = body,
            idToken = idToken,
            requiresAuth = requiresAuth,
            builder = builder,
        )
        if (response.status.value == nullStatusCode) return null
        requireSuccess(response = response, action = "$method $path")
        return parseResponse(response.bodyAsText())
    }

    suspend fun backendRequest(
        path: String,
        method: HttpMethod = HttpMethod.Get,
        body: Any? = null,
        idToken: String? = null,
        requiresAuth: Boolean = true,
        builder: HttpRequestBuilder.() -> Unit = {},
    ): HttpResponse {
        val resolvedToken = idToken ?: tokenProvider.getIdToken()
        val baseUrls = listOf(BackendEnvironment.primaryBaseUrl) + BackendEnvironment.fallbackBaseUrls
        var lastError: Throwable? = null

        baseUrls.forEachIndexed { index, baseUrl ->
            try {
                return httpClient.request {
                    url("$baseUrl$path")
                    this.method = method
                    if (requiresAuth) {
                        val token = resolvedToken ?: throw IllegalStateException("Missing auth token")
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }
                    if (body != null) {
                        setBody(body)
                        contentType(ContentType.Application.Json)
                    }
                    builder()
                }
            } catch (throwable: Throwable) {
                lastError = throwable
                val hasNext = index < baseUrls.lastIndex
                println(
                    "TradeBE: REQUEST $path failed base=$baseUrl retryNext=$hasNext error=${throwable.message.orEmpty()}"
                )
            }
        }

        throw lastError ?: IllegalStateException("Backend unavailable")
    }


    suspend fun requireSuccess(response: HttpResponse, action: String) {
        if (!response.status.isSuccess()) {
            val payload = runCatching { response.bodyAsText() }.getOrNull().orEmpty()
            val message = parseBackendError(payload)
            throw IllegalStateException(
                "$action failed (${response.status.value} ${response.status.description}). $message"
            )
        }
    }

    fun parseJsonObject(rawBody: String): JsonObject {
        val jsonBody = rawBody.trim()
        if (jsonBody.isBlank() || jsonBody == "null") return JsonObject(emptyMap())
        return json.parseToJsonElement(jsonBody) as? JsonObject ?: JsonObject(emptyMap())
    }

    fun parseJsonArray(rawBody: String): JsonArray {
        val jsonBody = rawBody.trim()
        if (jsonBody.isBlank() || jsonBody == "null") return JsonArray(emptyList())
        return json.parseToJsonElement(jsonBody) as? JsonArray ?: JsonArray(emptyList())
    }

    fun parseBackendError(rawBody: String, fallback: String = "Request failed"): String {
        val root = parseJsonObject(rawBody)
        return (root["message"] as? JsonPrimitive)
            ?.content
            ?.trim()
            ?.takeUnless { it.isBlank() }
            ?: fallback
    }

    inline fun <reified T> parseResponse(rawBody: String): T {
        return when (T::class) {
            Unit::class -> Unit as T
            JsonObject::class -> parseJsonObject(rawBody) as T
            JsonArray::class -> parseJsonArray(rawBody) as T
            String::class -> rawBody as T
            else -> json.decodeFromString(rawBody)
        }
    }
}
