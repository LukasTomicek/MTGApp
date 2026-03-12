package mtg.app.feature.chat.domain

import mtg.app.core.domain.obj.AuthContext
import mtg.app.feature.chat.domain.obj.DeleteChatThreadRequest
import mtg.app.feature.chat.domain.obj.SendChatMessageRequest
import mtg.app.feature.chat.domain.obj.SubmitChatRatingRequest

class DefaultChatService(
    private val repository: MessagesRepository,
) : ChatService {

    override suspend fun loadThreads(context: AuthContext): List<MessageThread> {
        return repository.loadThreads(context = context)
    }

    override suspend fun loadUserNickname(uid: String): String? {
        return repository.loadUserNickname(uid = uid)
    }

    override suspend fun deleteThread(context: AuthContext, request: DeleteChatThreadRequest) {
        repository.deleteThread(context = context, request = request)
    }

    override suspend fun loadChatMeta(context: AuthContext, chatId: String): ChatMeta? {
        return repository.loadChatMeta(context = context, chatId = chatId)
    }

    override suspend fun loadChatMessages(context: AuthContext, chatId: String): List<ChatMessage> {
        return repository.loadChatMessages(context = context, chatId = chatId)
    }

    override suspend fun sendMessage(context: AuthContext, request: SendChatMessageRequest) {
        repository.sendMessage(context = context, request = request)
    }

    override suspend fun proposeDeal(context: AuthContext, chatId: String) {
        repository.proposeDeal(context = context, chatId = chatId)
    }

    override suspend fun confirmDeal(context: AuthContext, chatId: String) {
        repository.confirmDeal(context = context, chatId = chatId)
    }

    override suspend fun hasRatedChat(context: AuthContext, chatId: String): Boolean {
        return repository.hasRatedChat(context = context, chatId = chatId)
    }

    override suspend fun loadUserRatingSummary(context: AuthContext, uid: String): UserRatingSummary {
        return repository.loadUserRatingSummary(context = context, uid = uid)
    }

    override suspend fun loadUserReviews(context: AuthContext, uid: String): List<UserReview> {
        return repository.loadUserReviews(context = context, uid = uid)
    }

    override suspend fun loadUserSellOffers(context: AuthContext, uid: String): List<UserSellOffer> {
        return repository.loadUserSellOffers(context = context, uid = uid)
    }

    override suspend fun submitRating(context: AuthContext, request: SubmitChatRatingRequest) {
        repository.submitRating(context = context, request = request)
    }
}
