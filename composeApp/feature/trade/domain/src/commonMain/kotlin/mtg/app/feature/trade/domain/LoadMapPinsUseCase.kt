package mtg.app.feature.trade.domain

class LoadMapPinsUseCase(
    private val repository: TradeRepository,
) {
    suspend operator fun invoke(uid: String, idToken: String): List<StoredMapPin> {
        return repository.loadMapPins(uid = uid, idToken = idToken)
    }
}
