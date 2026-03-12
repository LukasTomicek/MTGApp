package mtg.app.feature.chat.infrastructure

import mtg.app.feature.chat.data.MessagesDataSource
import mtg.app.feature.chat.domain.ChatMessage
import mtg.app.feature.chat.domain.ChatMeta
import mtg.app.feature.chat.domain.MessageThread
import mtg.app.feature.chat.domain.MessagesRepository
import mtg.app.feature.chat.domain.UserRatingSummary
import mtg.app.feature.chat.domain.UserReview
import mtg.app.feature.chat.domain.UserSellOffer
import mtg.app.core.domain.obj.AuthContext
import mtg.app.feature.chat.domain.obj.DeleteChatThreadRequest
import mtg.app.feature.chat.domain.obj.SendChatMessageRequest
import mtg.app.feature.chat.domain.obj.SubmitChatRatingRequest

class DefaultMessagesRepository(
    private val dataSource: MessagesDataSource,
) : MessagesRepository {
    override suspend fun loadThreads(context: AuthContext): List<MessageThread> {
        return dataSource.loadThreads(context = context)
    }

    override suspend fun loadUserNickname(uid: String): String? {
        return dataSource.loadUserNickname(uid = uid)
    }

    override suspend fun deleteThread(context: AuthContext, request: DeleteChatThreadRequest) {
        dataSource.deleteThread(context = context, request = request)
    }

    override suspend fun loadChatMeta(context: AuthContext, chatId: String): ChatMeta? {
        return dataSource.loadChatMeta(context = context, chatId = chatId)
    }

    override suspend fun loadChatMessages(context: AuthContext, chatId: String): List<ChatMessage> {
        return dataSource.loadChatMessages(context = context, chatId = chatId)
    }

    override suspend fun sendMessage(context: AuthContext, request: SendChatMessageRequest) {
        dataSource.sendMessage(context = context, request = request)
    }

    override suspend fun proposeDeal(context: AuthContext, chatId: String) {
        dataSource.proposeDeal(context = context, chatId = chatId)
    }

    override suspend fun confirmDeal(context: AuthContext, chatId: String) {
        dataSource.confirmDeal(context = context, chatId = chatId)
    }

    override suspend fun hasRatedChat(context: AuthContext, chatId: String): Boolean {
        return dataSource.hasRatedChat(context = context, chatId = chatId)
    }

    override suspend fun loadUserRatingSummary(context: AuthContext, uid: String): UserRatingSummary {
        return dataSource.loadUserRatingSummary(context = context, uid = uid)
    }

    override suspend fun loadUserReviews(context: AuthContext, uid: String): List<UserReview> {
        return dataSource.loadUserReviews(context = context, uid = uid)
    }

    override suspend fun loadUserSellOffers(context: AuthContext, uid: String): List<UserSellOffer> {
        return dataSource.loadUserSellOffers(context = context, uid = uid)
    }

    override suspend fun submitRating(context: AuthContext, request: SubmitChatRatingRequest) {
        dataSource.submitRating(context = context, request = request)
    }
}
