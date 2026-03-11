package mtg.app.feature.notifications.domain

class HasUnreadNotificationsUseCase(
    private val repository: NotificationsRepository,
) {
    suspend operator fun invoke(uid: String, idToken: String): Boolean {
        return repository.hasUnreadNotifications(uid = uid, idToken = idToken)
    }
}
