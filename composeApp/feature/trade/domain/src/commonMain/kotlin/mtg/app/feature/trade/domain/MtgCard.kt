package mtg.app.feature.trade.domain

data class MtgCard(
    val id: String,
    val name: String,
    val typeLine: String,
    val imageUrl: String?,
    val artDescriptor: String? = null,
)

enum class TradeFilter(val scryfallQuery: String?) {
    ALL(null),
    CREATURES("t:creature"),
    INSTANTS("t:instant"),
    SORCERIES("t:sorcery"),
    ARTIFACTS("t:artifact"),
}
