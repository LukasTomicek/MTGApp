package mtg.app.feature.chat.data

import mtg.app.feature.chat.domain.ChatMessage
import mtg.app.feature.chat.domain.ChatMeta
import mtg.app.feature.chat.domain.MessageThread
import mtg.app.feature.chat.domain.UserRatingSummary
import mtg.app.feature.chat.domain.UserReview
import mtg.app.feature.chat.domain.UserSellOffer

interface MessagesDataSource {
    suspend fun loadThreads(uid: String, idToken: String): List<MessageThread>
    suspend fun loadUserNickname(uid: String, idToken: String): String?
    suspend fun deleteThread(uid: String, idToken: String, chatId: String, counterpartUid: String)
    suspend fun loadChatMeta(chatId: String, idToken: String): ChatMeta?
    suspend fun loadChatMessages(chatId: String, idToken: String): List<ChatMessage>
    suspend fun sendMessage(
        uid: String,
        idToken: String,
        chatId: String,
        senderEmail: String,
        text: String,
    )
    suspend fun proposeDeal(uid: String, idToken: String, chatId: String)
    suspend fun confirmDeal(uid: String, idToken: String, chatId: String)
    suspend fun hasRatedChat(uid: String, idToken: String, chatId: String): Boolean
    suspend fun loadUserRatingSummary(uid: String, idToken: String): UserRatingSummary
    suspend fun loadUserReviews(uid: String, idToken: String): List<UserReview>
    suspend fun loadUserSellOffers(uid: String, idToken: String): List<UserSellOffer>
    suspend fun submitRating(
        raterUid: String,
        ratedUid: String,
        chatId: String,
        idToken: String,
        score: Int,
        comment: String,
    )
}
