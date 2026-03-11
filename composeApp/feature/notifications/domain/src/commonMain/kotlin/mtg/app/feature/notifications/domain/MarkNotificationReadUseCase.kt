package mtg.app.feature.notifications.domain

class MarkNotificationReadUseCase(
    private val repository: NotificationsRepository,
) {
    suspend operator fun invoke(uid: String, idToken: String, notificationId: String) {
        repository.markNotificationRead(
            uid = uid,
            idToken = idToken,
            notificationId = notificationId,
        )
    }
}
