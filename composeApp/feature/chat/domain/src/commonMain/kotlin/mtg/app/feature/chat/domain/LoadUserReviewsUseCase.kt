package mtg.app.feature.chat.domain

class LoadUserReviewsUseCase(
    private val repository: MessagesRepository,
) {
    suspend operator fun invoke(uid: String, idToken: String): List<UserReview> {
        return repository.loadUserReviews(uid = uid, idToken = idToken)
    }
}
