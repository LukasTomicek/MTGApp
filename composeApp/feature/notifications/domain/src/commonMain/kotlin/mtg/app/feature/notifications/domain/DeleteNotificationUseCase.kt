package mtg.app.feature.notifications.domain

class DeleteNotificationUseCase(
    private val repository: NotificationsRepository,
) {
    suspend operator fun invoke(uid: String, idToken: String, notificationId: String) {
        repository.deleteNotification(
            uid = uid,
            idToken = idToken,
            notificationId = notificationId,
        )
    }
}
