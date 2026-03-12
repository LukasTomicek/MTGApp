package mtg.app.feature.chat.domain.obj

data class DeleteChatThreadRequest(
    val chatId: String,
    val counterpartUid: String,
)

data class SendChatMessageRequest(
    val chatId: String,
    val senderEmail: String,
    val text: String,
)

data class SubmitChatRatingRequest(
    val ratedUid: String,
    val chatId: String,
    val score: Int,
    val comment: String,
)
