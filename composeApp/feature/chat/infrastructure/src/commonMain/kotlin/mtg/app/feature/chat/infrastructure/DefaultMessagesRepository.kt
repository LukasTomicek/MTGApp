package mtg.app.feature.chat.infrastructure

import mtg.app.feature.chat.data.MessagesDataSource
import mtg.app.feature.chat.domain.ChatMessage
import mtg.app.feature.chat.domain.ChatMeta
import mtg.app.feature.chat.domain.MessageThread
import mtg.app.feature.chat.domain.MessagesRepository
import mtg.app.feature.chat.domain.UserRatingSummary
import mtg.app.feature.chat.domain.UserReview
import mtg.app.feature.chat.domain.UserSellOffer

class DefaultMessagesRepository(
    private val dataSource: MessagesDataSource,
) : MessagesRepository {
    override suspend fun loadThreads(uid: String, idToken: String): List<MessageThread> {
        return dataSource.loadThreads(uid = uid, idToken = idToken)
    }

    override suspend fun loadUserNickname(uid: String, idToken: String): String? {
        return dataSource.loadUserNickname(uid = uid, idToken = idToken)
    }

    override suspend fun deleteThread(uid: String, idToken: String, chatId: String, counterpartUid: String) {
        dataSource.deleteThread(
            uid = uid,
            idToken = idToken,
            chatId = chatId,
            counterpartUid = counterpartUid,
        )
    }

    override suspend fun loadChatMeta(chatId: String, idToken: String): ChatMeta? {
        return dataSource.loadChatMeta(chatId = chatId, idToken = idToken)
    }

    override suspend fun loadChatMessages(chatId: String, idToken: String): List<ChatMessage> {
        return dataSource.loadChatMessages(chatId = chatId, idToken = idToken)
    }

    override suspend fun sendMessage(
        uid: String,
        idToken: String,
        chatId: String,
        senderEmail: String,
        text: String,
    ) {
        dataSource.sendMessage(
            uid = uid,
            idToken = idToken,
            chatId = chatId,
            senderEmail = senderEmail,
            text = text,
        )
    }

    override suspend fun proposeDeal(uid: String, idToken: String, chatId: String) {
        dataSource.proposeDeal(uid = uid, idToken = idToken, chatId = chatId)
    }

    override suspend fun confirmDeal(uid: String, idToken: String, chatId: String) {
        dataSource.confirmDeal(uid = uid, idToken = idToken, chatId = chatId)
    }

    override suspend fun hasRatedChat(uid: String, idToken: String, chatId: String): Boolean {
        return dataSource.hasRatedChat(uid = uid, idToken = idToken, chatId = chatId)
    }

    override suspend fun loadUserRatingSummary(uid: String, idToken: String): UserRatingSummary {
        return dataSource.loadUserRatingSummary(uid = uid, idToken = idToken)
    }

    override suspend fun loadUserReviews(uid: String, idToken: String): List<UserReview> {
        return dataSource.loadUserReviews(uid = uid, idToken = idToken)
    }

    override suspend fun loadUserSellOffers(uid: String, idToken: String): List<UserSellOffer> {
        return dataSource.loadUserSellOffers(uid = uid, idToken = idToken)
    }

    override suspend fun submitRating(
        raterUid: String,
        ratedUid: String,
        chatId: String,
        idToken: String,
        score: Int,
        comment: String,
    ) {
        dataSource.submitRating(
            raterUid = raterUid,
            ratedUid = ratedUid,
            chatId = chatId,
            idToken = idToken,
            score = score,
            comment = comment,
        )
    }
}
