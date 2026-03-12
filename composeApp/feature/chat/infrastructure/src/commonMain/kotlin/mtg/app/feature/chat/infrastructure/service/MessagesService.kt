package mtg.app.feature.chat.infrastructure.service

import kotlinx.serialization.json.JsonObject

interface MessagesService {
    suspend fun listUserMatches(uid: String, idToken: String): JsonObject
    suspend fun listChats(idToken: String): JsonObject
    suspend fun listMarketplaceSellEntriesByUser(uid: String, idToken: String): JsonObject
    suspend fun deleteThread(uid: String, idToken: String, chatId: String, counterpartUid: String)
    suspend fun getChatMeta(chatId: String, idToken: String): JsonObject?
    suspend fun listChatMessages(chatId: String, idToken: String): JsonObject
    suspend fun loadUserNickname(uid: String, idToken: String): String?
    suspend fun sendMessage(
        uid: String,
        idToken: String,
        chatId: String,
        senderEmail: String,
        text: String,
    )
    suspend fun proposeDeal(uid: String, chatId: String, idToken: String)
    suspend fun confirmDeal(uid: String, chatId: String, idToken: String)
    suspend fun hasRatedChat(uid: String, chatId: String, idToken: String): Boolean
    suspend fun loadUserReceivedRatings(uid: String, idToken: String): JsonObject
    suspend fun submitRating(
        chatId: String,
        idToken: String,
        ratedUid: String,
        score: Int,
        comment: String,
    )
}
