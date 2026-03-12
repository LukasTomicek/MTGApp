package mtg.app.feature.chat.data

import mtg.app.feature.chat.domain.ChatMessage
import mtg.app.feature.chat.domain.ChatMeta
import mtg.app.feature.chat.domain.MessageThread
import mtg.app.feature.chat.domain.UserRatingSummary
import mtg.app.feature.chat.domain.UserReview
import mtg.app.feature.chat.domain.UserSellOffer
import mtg.app.core.domain.obj.AuthContext
import mtg.app.feature.chat.domain.obj.DeleteChatThreadRequest
import mtg.app.feature.chat.domain.obj.SendChatMessageRequest
import mtg.app.feature.chat.domain.obj.SubmitChatRatingRequest

interface MessagesDataSource {
    suspend fun loadThreads(context: AuthContext): List<MessageThread>
    suspend fun loadUserNickname(uid: String): String?
    suspend fun deleteThread(context: AuthContext, request: DeleteChatThreadRequest)
    suspend fun loadChatMeta(context: AuthContext, chatId: String): ChatMeta?
    suspend fun loadChatMessages(context: AuthContext, chatId: String): List<ChatMessage>
    suspend fun sendMessage(context: AuthContext, request: SendChatMessageRequest)
    suspend fun proposeDeal(context: AuthContext, chatId: String)
    suspend fun confirmDeal(context: AuthContext, chatId: String)
    suspend fun hasRatedChat(context: AuthContext, chatId: String): Boolean
    suspend fun loadUserRatingSummary(context: AuthContext, uid: String): UserRatingSummary
    suspend fun loadUserReviews(context: AuthContext, uid: String): List<UserReview>
    suspend fun loadUserSellOffers(context: AuthContext, uid: String): List<UserSellOffer>
    suspend fun submitRating(context: AuthContext, request: SubmitChatRatingRequest)
}
