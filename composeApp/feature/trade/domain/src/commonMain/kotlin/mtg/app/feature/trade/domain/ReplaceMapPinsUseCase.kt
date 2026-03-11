package mtg.app.feature.trade.domain

class ReplaceMapPinsUseCase(
    private val repository: TradeRepository,
) {
    suspend operator fun invoke(
        uid: String,
        idToken: String,
        pins: List<StoredMapPin>,
        actorEmail: String? = null,
        triggerRematch: Boolean = true,
    ) {
        repository.replaceMapPins(
            uid = uid,
            idToken = idToken,
            pins = pins,
            actorEmail = actorEmail,
            triggerRematch = triggerRematch,
        )
    }
}
