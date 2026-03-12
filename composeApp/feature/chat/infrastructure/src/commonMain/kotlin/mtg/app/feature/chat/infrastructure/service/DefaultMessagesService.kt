package mtg.app.feature.chat.infrastructure.service

import mtg.app.core.domain.config.BackendEnvironment
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.encodeURLPath
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class DefaultMessagesService(
    private val httpClient: HttpClient,
) : MessagesService {
    private val tradeBackendBaseUrl = BackendEnvironment.primaryBaseUrl

    override suspend fun listUserMatches(uid: String, idToken: String): JsonObject {
        println("TradeBE: calling /v1/users/me/matches")
        val response = httpClient.get("$tradeBackendBaseUrl/v1/users/me/matches") {
            withBearer(idToken)
        }
        val json = response.bodyAsText().trim()
        return if (json == "null" || json.isBlank()) JsonObject(emptyMap()) else {
            Json.parseToJsonElement(json) as? JsonObject ?: JsonObject(emptyMap())
        }
    }

    override suspend fun listChats(idToken: String): JsonObject {
        println("TradeBE: calling /v1/chats")
        val response = httpClient.get("$tradeBackendBaseUrl/v1/chats") {
            withBearer(idToken)
        }
        val json = response.bodyAsText().trim()
        return if (json == "null" || json.isBlank()) JsonObject(emptyMap()) else {
            Json.parseToJsonElement(json) as? JsonObject ?: JsonObject(emptyMap())
        }
    }

    override suspend fun listMarketplaceSellEntriesByUser(uid: String, idToken: String): JsonObject {
        val user = uid.encodeURLPath()
        println("TradeBE: calling /v1/users/$uid/sell-offers")
        val response = httpClient.get("$tradeBackendBaseUrl/v1/users/$user/sell-offers") {
            withBearer(idToken)
        }
        val json = response.bodyAsText().trim()
        return if (json == "null" || json.isBlank()) JsonObject(emptyMap()) else {
            Json.parseToJsonElement(json) as? JsonObject ?: JsonObject(emptyMap())
        }
    }

    override suspend fun getChatMeta(chatId: String, idToken: String): JsonObject? {
        val id = chatId.encodeURLPath()
        println("TradeBE: calling /v1/chats/$chatId")
        val response = httpClient.get("$tradeBackendBaseUrl/v1/chats/$id") {
            withBearer(idToken)
        }
        if (response.status.value == 404) return null
        val json = response.bodyAsText().trim()
        if (json == "null" || json.isBlank()) return null
        return Json.parseToJsonElement(json) as? JsonObject
    }

    override suspend fun deleteThread(
        uid: String,
        idToken: String,
        chatId: String,
        counterpartUid: String,
    ) {
        val encodedChatId = chatId.encodeURLPath()
        println("TradeBE: calling DELETE /v1/chats/$chatId")
        val response = httpClient.delete("$tradeBackendBaseUrl/v1/chats/$encodedChatId") {
            withBearer(idToken)
        }
        ensureSuccess(response, "delete chat thread")
    }

    override suspend fun listChatMessages(chatId: String, idToken: String): JsonObject {
        val id = chatId.encodeURLPath()
        println("TradeBE: calling /v1/chats/$chatId/messages")
        val response = httpClient.get("$tradeBackendBaseUrl/v1/chats/$id/messages") {
            withBearer(idToken)
        }
        val json = response.bodyAsText().trim()
        return if (json == "null" || json.isBlank()) JsonObject(emptyMap()) else {
            Json.parseToJsonElement(json) as? JsonObject ?: JsonObject(emptyMap())
        }
    }

    override suspend fun sendMessage(
        uid: String,
        idToken: String,
        chatId: String,
        senderEmail: String,
        text: String,
    ) {
        val normalizedText = text.trim()
        if (normalizedText.isBlank()) return

        val senderDisplayName = runCatching {
            loadUserNickname(uid = uid, idToken = idToken)
        }.onFailure { throwable ->
            println("TradeBE: loadUserNickname failed before send, fallback to uid. error=${throwable.message}")
        }.getOrNull()
            ?.trim()
            .orEmpty()
            .ifBlank { senderEmail.trim().ifBlank { uid } }
        val encodedChatId = chatId.encodeURLPath()

        println("TradeBE: calling /v1/chats/$chatId/messages")
        val response = runCatching {
            httpClient.post("$tradeBackendBaseUrl/v1/chats/$encodedChatId/messages") {
                withBearer(idToken)
                contentType(ContentType.Application.Json)
                setBody(
                    buildJsonObject {
                        put("senderDisplayName", senderDisplayName)
                        put("text", normalizedText)
                    }
                )
            }
        }.onFailure { throwable ->
            println("TradeBE: POST /messages failed before response. chatId=$chatId error=${throwable.message}")
        }.getOrElse { throw it }
        println("TradeBE: /messages response status=${response.status.value} chatId=$chatId")
        ensureSuccess(response, "send message")
        println("TradeBE: /messages success chatId=$chatId")
    }

    override suspend fun loadUserNickname(
        uid: String,
        idToken: String,
    ): String? {
        val pathUid = uid.encodeURLPath()
        val pathResponse = runCatching {
            httpClient.get("$tradeBackendBaseUrl/v1/users/profile/$pathUid")
        }.getOrNull() ?: return null
        if (!pathResponse.status.isSuccess()) return null
        val pathBody = pathResponse.bodyAsText().trim()
        val root = runCatching { Json.parseToJsonElement(pathBody) as? JsonObject }.getOrNull() ?: return null
        return root.stringOrNull("nickname")
    }

    override suspend fun proposeDeal(uid: String, chatId: String, idToken: String) {
        val meta = getChatMeta(chatId = chatId, idToken = idToken)
            ?: throw IllegalStateException("Chat not found")
        val buyerUid = meta.getString("buyerUid").orEmpty()
        if (uid != buyerUid) {
            throw IllegalStateException("Only buyer can reserve deal")
        }

        val encodedChatId = chatId.encodeURLPath()
        val response = httpClient.patch("$tradeBackendBaseUrl/v1/chats/$encodedChatId") {
            withBearer(idToken)
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonObject {
                    put("dealStatus", "PROPOSED")
                    put("buyerConfirmed", true)
                    put("sellerConfirmed", false)
                    put("buyerCompleted", false)
                    put("sellerCompleted", false)
                }
            )
        }
        ensureSuccess(response, "propose deal")
    }

    override suspend fun confirmDeal(uid: String, chatId: String, idToken: String) {
        val meta = getChatMeta(chatId = chatId, idToken = idToken)
            ?: throw IllegalStateException("Chat not found")
        val buyerUid = meta.getString("buyerUid").orEmpty()
        val sellerUid = meta.getString("sellerUid").orEmpty()
        if (uid != buyerUid && uid != sellerUid) {
            throw IllegalStateException("Only chat participants can confirm deal")
        }

        val dealStatus = meta.getString("dealStatus").orEmpty()
        if (!dealStatus.equals("PROPOSED", ignoreCase = true)) {
            throw IllegalStateException("Deal must be in PROPOSED state")
        }
        val buyerConfirmed = meta.getBoolean("buyerConfirmed") == true
        val sellerConfirmed = meta.getBoolean("sellerConfirmed") == true
        val buyerCompleted = meta.getBoolean("buyerCompleted") == true
        val sellerCompleted = meta.getBoolean("sellerCompleted") == true
        if (!buyerConfirmed) {
            throw IllegalStateException("Buyer must reserve deal first")
        }

        val encodedChatId = chatId.encodeURLPath()
        if (!sellerConfirmed) {
            if (uid != sellerUid) {
                throw IllegalStateException("Only seller can confirm reserved deal")
            }
            val confirmResponse = httpClient.patch("$tradeBackendBaseUrl/v1/chats/$encodedChatId") {
                withBearer(idToken)
                contentType(ContentType.Application.Json)
                setBody(
                    buildJsonObject {
                        put("dealStatus", "PROPOSED")
                        put("buyerConfirmed", true)
                        put("sellerConfirmed", true)
                        put("buyerCompleted", false)
                        put("sellerCompleted", false)
                    }
                )
            }
            ensureSuccess(confirmResponse, "confirm deal")
            return
        }

        val markBuyerCompleted = if (uid == buyerUid) true else buyerCompleted
        val markSellerCompleted = if (uid == sellerUid) true else sellerCompleted
        if (uid != buyerUid && uid != sellerUid) {
            throw IllegalStateException("Only participants can complete deal")
        }

        val shouldFinalize = markBuyerCompleted && markSellerCompleted
        val completeStatus = if (shouldFinalize) "COMPLETED" else "PROPOSED"

        val completeResponse = httpClient.patch("$tradeBackendBaseUrl/v1/chats/$encodedChatId") {
            withBearer(idToken)
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonObject {
                    put("dealStatus", completeStatus)
                    put("buyerConfirmed", true)
                    put("sellerConfirmed", true)
                    put("buyerCompleted", markBuyerCompleted)
                    put("sellerCompleted", markSellerCompleted)
                    if (shouldFinalize) {
                        put("closed", true)
                        put("closedAt", nowMillis())
                        put("offerState", "SOLD")
                    }
                }
            )
        }
        ensureSuccess(completeResponse, "complete deal")
    }

    override suspend fun hasRatedChat(uid: String, chatId: String, idToken: String): Boolean {
        val chat = chatId.encodeURLPath()
        val response = httpClient.get("$tradeBackendBaseUrl/v1/users/me/ratings/given/$chat") {
            withBearer(idToken)
        }
        val body = response.bodyAsText().trim()
        val root = runCatching { Json.parseToJsonElement(body) as? JsonObject }.getOrNull() ?: return false
        return (root["exists"] as? JsonPrimitive)?.content?.toBooleanStrictOrNull() == true
    }

    override suspend fun loadUserReceivedRatings(uid: String, idToken: String): JsonObject {
        val user = uid.encodeURLPath()
        val response = httpClient.get("$tradeBackendBaseUrl/v1/users/$user/ratings") {
            withBearer(idToken)
        }
        val json = response.bodyAsText().trim()
        return if (json == "null" || json.isBlank()) JsonObject(emptyMap()) else {
            Json.parseToJsonElement(json) as? JsonObject ?: JsonObject(emptyMap())
        }
    }

    override suspend fun submitRating(
        chatId: String,
        idToken: String,
        ratedUid: String,
        score: Int,
        comment: String,
    ) {
        val encodedChatId = chatId.encodeURLPath()
        val response = httpClient.post("$tradeBackendBaseUrl/v1/chats/$encodedChatId/ratings") {
            withBearer(idToken)
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonObject {
                    put("ratedUid", ratedUid)
                    put("score", score)
                    put("comment", comment)
                }
            )
        }
        ensureSuccess(response, "submit rating")
    }
}

private fun nowMillis(): Long = kotlin.time.Clock.System.now().toEpochMilliseconds()

private suspend fun ensureSuccess(response: HttpResponse, action: String) {
    if (!response.status.isSuccess()) {
        val payload = runCatching { response.bodyAsText() }.getOrNull().orEmpty()
        throw IllegalStateException(
            "Failed to $action (${response.status.value} ${response.status.description}). $payload"
        )
    }
}

private fun HttpRequestBuilder.withBearer(idToken: String) {
    header(HttpHeaders.Authorization, "Bearer $idToken")
}

private fun JsonObject?.getString(key: String): String? {
    val obj = this ?: return null
    val primitive = obj[key] as? JsonPrimitive ?: return null
    return runCatching { primitive.content }.getOrNull()
}

private fun JsonObject?.getBoolean(key: String): Boolean? {
    val obj = this ?: return null
    val primitive = obj[key] as? JsonPrimitive ?: return null
    return primitive.content.toBooleanStrictOrNull()
}

private fun JsonObject?.stringOrNull(key: String): String? {
    val element = this?.get(key) ?: return null
    if (element is JsonNull) return null
    val primitive = element as? JsonPrimitive ?: return null
    return runCatching { primitive.content }.getOrNull()?.trim()?.takeUnless { it.isBlank() }
}
