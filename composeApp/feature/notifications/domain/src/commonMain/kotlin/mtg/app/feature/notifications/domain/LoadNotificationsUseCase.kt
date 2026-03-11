package mtg.app.feature.notifications.domain

class LoadNotificationsUseCase(
    private val repository: NotificationsRepository,
) {
    suspend operator fun invoke(uid: String, idToken: String): List<NotificationItem> {
        return repository.loadNotifications(uid = uid, idToken = idToken)
    }
}
