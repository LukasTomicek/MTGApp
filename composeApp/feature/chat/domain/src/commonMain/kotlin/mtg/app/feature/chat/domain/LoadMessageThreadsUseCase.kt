package mtg.app.feature.chat.domain

class LoadMessageThreadsUseCase(
    private val repository: MessagesRepository,
) {
    suspend operator fun invoke(uid: String, idToken: String): List<MessageThread> {
        return repository.loadThreads(uid = uid, idToken = idToken)
    }
}
