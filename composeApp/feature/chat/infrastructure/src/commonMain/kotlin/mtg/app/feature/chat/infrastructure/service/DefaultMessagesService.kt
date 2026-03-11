package mtg.app.feature.chat.infrastructure.service

import mtg.app.core.domain.config.BackendEnvironment
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodeURLPath
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class DefaultMessagesService(
    private val httpClient: HttpClient,
) : MessagesService {
    private val tradeBackendBaseUrl = BackendEnvironment.primaryBaseUrl

    override suspend fun listUserMatches(uid: String, idToken: String): JsonObject {
        val user = uid.encodeURLPath()
        println("TradeBE: calling /v1/bridge/users/$uid/matches")
        val response = httpClient.get("$tradeBackendBaseUrl/v1/bridge/users/$user/matches")
        val json = response.bodyAsText().trim()
        return if (json == "null" || json.isBlank()) JsonObject(emptyMap()) else {
            Json.parseToJsonElement(json) as? JsonObject ?: JsonObject(emptyMap())
        }
    }

    override suspend fun listChats(idToken: String): JsonObject {
        println("TradeBE: calling /v1/bridge/chats")
        val response = httpClient.get("$tradeBackendBaseUrl/v1/bridge/chats")
        val json = response.bodyAsText().trim()
        return if (json == "null" || json.isBlank()) JsonObject(emptyMap()) else {
            Json.parseToJsonElement(json) as? JsonObject ?: JsonObject(emptyMap())
        }
    }

    override suspend fun listMarketplaceSellEntriesByUser(uid: String, idToken: String): JsonObject {
        val user = uid.encodeURLPath()
        println("TradeBE: calling /v1/bridge/market/sell-offers/$uid")
        val response = httpClient.get("$tradeBackendBaseUrl/v1/bridge/market/sell-offers/$user")
        val json = response.bodyAsText().trim()
        return if (json == "null" || json.isBlank()) JsonObject(emptyMap()) else {
            Json.parseToJsonElement(json) as? JsonObject ?: JsonObject(emptyMap())
        }
    }

    override suspend fun getChatMeta(chatId: String, idToken: String): JsonObject? {
        val id = chatId.encodeURLPath()
        println("TradeBE: calling /v1/bridge/chats/$chatId/meta")
        val response = httpClient.get("$tradeBackendBaseUrl/v1/bridge/chats/$id/meta")
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
        println("TradeBE: calling /v1/bridge/chats/$chatId/thread DELETE")
        val response = httpClient.delete("$tradeBackendBaseUrl/v1/bridge/chats/$encodedChatId/thread") {
            parameter("uid", uid)
            parameter("counterpartUid", counterpartUid)
        }
        ensureSuccess(response, "delete chat thread")
    }

    override suspend fun listChatMessages(chatId: String, idToken: String): JsonObject {
        val id = chatId.encodeURLPath()
        println("TradeBE: calling /v1/bridge/chats/$chatId/messages")
        val response = httpClient.get("$tradeBackendBaseUrl/v1/bridge/chats/$id/messages")
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

        println("TradeBE: calling /v1/bridge/chats/$chatId/messages/send")
        val response = runCatching {
            httpClient.post("$tradeBackendBaseUrl/v1/bridge/chats/$encodedChatId/messages/send") {
                contentType(ContentType.Application.Json)
                setBody(
                    buildJsonObject {
                        put("uid", uid)
                        put("senderDisplayName", senderDisplayName)
                        put("text", normalizedText)
                    }
                )
            }
        }.onFailure { throwable ->
            println("TradeBE: POST /messages/send failed before response. chatId=$chatId error=${throwable.message}")
        }.getOrElse { throw it }
        println("TradeBE: /messages/send response status=${response.status.value} chatId=$chatId")
        ensureSuccess(response, "send message")
        println("TradeBE: /messages/send success chatId=$chatId")
    }

    override suspend fun loadUserNickname(
        uid: String,
        idToken: String,
    ): String? {
        val queryResponse = runCatching {
            httpClient.get("$tradeBackendBaseUrl/v1/users/profile") {
                parameter("userId", uid)
            }
        }.getOrNull()
        if (queryResponse?.status?.isSuccess() == true) {
            val queryBody = queryResponse.bodyAsText().trim()
            val root = runCatching { Json.parseToJsonElement(queryBody) as? JsonObject }.getOrNull()
            val nickname = root.stringOrNull("nickname")
            if (!nickname.isNullOrBlank()) return nickname
        }

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
        val response = httpClient.patch("$tradeBackendBaseUrl/v1/bridge/chats/$encodedChatId/meta") {
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
        val cardId = meta.getString("cardId").orEmpty()
        val cardName = meta.getString("cardName").orEmpty()
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
            val confirmResponse = httpClient.patch("$tradeBackendBaseUrl/v1/bridge/chats/$encodedChatId/meta") {
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

        val completeResponse = httpClient.patch("$tradeBackendBaseUrl/v1/bridge/chats/$encodedChatId/meta") {
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

        if (!shouldFinalize) return

        runCatching {
            removeSellerOffersForCard(
                httpClient = httpClient,
                tradeBackendBaseUrl = tradeBackendBaseUrl,
                sellerUid = sellerUid,
                cardId = cardId,
                cardName = cardName,
            )
        }
    }

    override suspend fun hasRatedChat(uid: String, chatId: String, idToken: String): Boolean {
        val user = uid.encodeURLPath()
        val chat = chatId.encodeURLPath()
        val response = httpClient.get("$tradeBackendBaseUrl/v1/bridge/users/$user/ratings/given/$chat")
        val body = response.bodyAsText().trim()
        val root = runCatching { Json.parseToJsonElement(body) as? JsonObject }.getOrNull() ?: return false
        return (root["exists"] as? JsonPrimitive)?.content?.toBooleanStrictOrNull() == true
    }

    override suspend fun loadUserReceivedRatings(uid: String, idToken: String): JsonObject {
        val user = uid.encodeURLPath()
        val response = httpClient.get("$tradeBackendBaseUrl/v1/bridge/users/$user/ratings/received")
        val json = response.bodyAsText().trim()
        return if (json == "null" || json.isBlank()) JsonObject(emptyMap()) else {
            Json.parseToJsonElement(json) as? JsonObject ?: JsonObject(emptyMap())
        }
    }

    override suspend fun saveGivenRating(uid: String, chatId: String, idToken: String, payload: JsonObject) {
        val user = uid.encodeURLPath()
        val chat = chatId.encodeURLPath()
        val response = httpClient.put("$tradeBackendBaseUrl/v1/bridge/users/$user/ratings/given/$chat") {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        ensureSuccess(response, "save given rating")
    }

    override suspend fun saveReceivedRating(
        ratedUid: String,
        ratingId: String,
        idToken: String,
        payload: JsonObject,
    ) {
        val rated = ratedUid.encodeURLPath()
        val id = ratingId.encodeURLPath()
        val response = httpClient.put("$tradeBackendBaseUrl/v1/bridge/users/$rated/ratings/received/$id") {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        ensureSuccess(response, "save received rating")
    }

    override suspend fun updateUserProfileRating(
        uid: String,
        idToken: String,
        average: Double,
        count: Int,
    ) {
        val user = uid.encodeURLPath()
        val response = httpClient.put("$tradeBackendBaseUrl/v1/bridge/users/$user/profile/rating") {
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonObject {
                    put("average", average)
                    put("count", count)
                }
            )
        }
        ensureSuccess(response, "update user profile rating")
    }

    private suspend fun deleteChatNotifications(uid: String, chatId: String, idToken: String) {
        val user = uid.encodeURLPath()
        val notificationsResponse = httpClient.get("$tradeBackendBaseUrl/v1/bridge/users/$user/notifications")
        if (!notificationsResponse.status.isSuccess()) return

        val notificationsRaw = notificationsResponse.bodyAsText().trim()
        val notifications = if (notificationsRaw == "null" || notificationsRaw.isBlank()) {
            JsonObject(emptyMap())
        } else {
            Json.parseToJsonElement(notificationsRaw) as? JsonObject ?: JsonObject(emptyMap())
        }

        notifications.entries.forEach { (notificationId, element) ->
            val notification = element as? JsonObject ?: return@forEach
            val notificationChatId = (notification["chatId"] as? JsonPrimitive)?.content.orEmpty()
            if (notificationChatId != chatId) return@forEach
            runCatching {
                httpClient.delete("$tradeBackendBaseUrl/v1/bridge/users/$user/notifications/${notificationId.encodeURLPath()}")
            }
        }
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

private suspend fun removeSellerOffersForCard(
    httpClient: HttpClient,
    tradeBackendBaseUrl: String,
    sellerUid: String,
    cardId: String,
    cardName: String,
) {
    val response = httpClient.get("$tradeBackendBaseUrl/v1/offers") {
        parameter("userId", sellerUid)
        parameter("type", "SELL")
    }
    ensureSuccess(response, "load seller offers before cleanup")

    val raw = response.bodyAsText().trim()
    val items = runCatching { Json.parseToJsonElement(raw) as? JsonArray }.getOrNull().orEmpty()
    val targetCardId = cardId.trim()
    val targetCardName = cardName.trim()

    items.forEach { element ->
        val obj = element as? JsonObject ?: return@forEach
        val offerId = (obj["id"] as? JsonPrimitive)?.content?.trim().orEmpty()
        if (offerId.isBlank()) return@forEach

        val offerCardId = (obj["cardId"] as? JsonPrimitive)?.content?.trim().orEmpty()
        val offerCardName = (obj["cardName"] as? JsonPrimitive)?.content?.trim().orEmpty()
        val matchesCard = when {
            targetCardId.isNotBlank() -> offerCardId.equals(targetCardId, ignoreCase = true)
            else -> offerCardName.equals(targetCardName, ignoreCase = true)
        }
        if (!matchesCard) return@forEach

        val deleteResponse = httpClient.delete("$tradeBackendBaseUrl/v1/offers/${offerId.encodeURLPath()}")
        ensureSuccess(deleteResponse, "remove seller offer after completed deal")
    }
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
