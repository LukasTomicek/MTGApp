package mtg.app.feature.trade.presentation.utils.mapper

import mtg.app.feature.trade.domain.MtgCard
import mtg.app.feature.trade.domain.StoredTradeCardEntry
import mtg.app.feature.trade.presentation.utils.model.CardCondition
import mtg.app.feature.trade.presentation.utils.model.CollectionCardEntry
import mtg.app.feature.trade.presentation.utils.model.FoilOption
import mtg.app.feature.trade.presentation.utils.model.LanguageOption

fun CollectionCardEntry.toStoredTradeCardEntry(): StoredTradeCardEntry {
    return StoredTradeCardEntry(
        entryId = entryId,
        cardId = card.id,
        cardName = card.name,
        cardTypeLine = card.typeLine,
        cardImageUrl = card.imageUrl,
        cardArtDescriptor = card.artDescriptor,
        quantity = quantity,
        foil = foil.name,
        language = language.name,
        condition = condition.name,
        price = price,
        artLabel = artLabel,
        artImageUrl = artImageUrl,
    )
}

fun StoredTradeCardEntry.toCollectionCardEntry(): CollectionCardEntry {
    return CollectionCardEntry(
        entryId = entryId,
        card = MtgCard(
            id = cardId,
            name = cardName,
            typeLine = cardTypeLine,
            imageUrl = cardImageUrl,
            artDescriptor = cardArtDescriptor,
        ),
        quantity = quantity,
        foil = enumValueOrDefault(foil, FoilOption.NON_FOIL),
        language = enumValueOrDefault(language, LanguageOption.EN),
        condition = enumValueOrDefault(condition, CardCondition.NM),
        price = price,
        artLabel = artLabel,
        artImageUrl = artImageUrl,
    )
}

private inline fun <reified T : Enum<T>> enumValueOrDefault(
    raw: String,
    fallback: T,
): T {
    return enumValues<T>().firstOrNull { it.name == raw } ?: fallback
}
