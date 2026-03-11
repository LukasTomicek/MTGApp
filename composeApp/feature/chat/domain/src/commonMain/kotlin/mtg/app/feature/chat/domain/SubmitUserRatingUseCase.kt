package mtg.app.feature.chat.domain

class SubmitUserRatingUseCase(
    private val repository: MessagesRepository,
) {
    suspend operator fun invoke(
        raterUid: String,
        ratedUid: String,
        chatId: String,
        idToken: String,
        score: Int,
        comment: String,
    ) {
        repository.submitRating(
            raterUid = raterUid,
            ratedUid = ratedUid,
            chatId = chatId,
            idToken = idToken,
            score = score,
            comment = comment,
        )
    }
}
