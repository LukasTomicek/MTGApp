package mtg.app.feature.chat.domain

class LoadUserRatingSummaryUseCase(
    private val repository: MessagesRepository,
) {
    suspend operator fun invoke(uid: String, idToken: String): UserRatingSummary {
        return repository.loadUserRatingSummary(uid = uid, idToken = idToken)
    }
}
