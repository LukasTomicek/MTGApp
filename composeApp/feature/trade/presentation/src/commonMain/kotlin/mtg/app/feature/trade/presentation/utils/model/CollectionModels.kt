package mtg.app.feature.trade.presentation.utils.model

import mtg.app.feature.trade.domain.MtgCard

enum class FoilOption(val label: String) {
    NON_FOIL("Non-foil"),
    FOIL("Foil"),
}

enum class LanguageOption(val label: String) {
    EN("EN"),
    DE("DE"),
    CZ("CZ"),
    FR("FR"),
}

enum class CardCondition(val label: String) {
    NM("NM"),
    EXC("EXC"),
    PL("PL"),
    POOR("POOR"),
}

data class CollectionCardEntry(
    val entryId: String,
    val card: MtgCard,
    val quantity: Int,
    val foil: FoilOption,
    val language: LanguageOption,
    val condition: CardCondition,
    val price: Double? = null,
    val artLabel: String,
    val artImageUrl: String?,
)

data class CollectionArtOption(
    val id: String,
    val label: String,
    val imageUrl: String?,
)
