package mtg.app.feature.trade.presentation.utils.model

fun CollectionCardEntry.hasSameMergeIdentityAs(other: CollectionCardEntry): Boolean {
    return mergeIdentity() == other.mergeIdentity()
}

fun mergeDuplicateEntries(entries: List<CollectionCardEntry>): List<CollectionCardEntry> {
    if (entries.size < 2) return entries

    val merged = mutableListOf<CollectionCardEntry>()
    val indexByIdentity = mutableMapOf<MergeIdentity, Int>()

    entries.forEach { entry ->
        val normalizedEntry = entry.copy(quantity = entry.quantity.coerceAtLeast(1))
        val identity = normalizedEntry.mergeIdentity()
        val existingIndex = indexByIdentity[identity]

        if (existingIndex == null) {
            indexByIdentity[identity] = merged.size
            merged += normalizedEntry
        } else {
            val existing = merged[existingIndex]
            merged[existingIndex] = existing.copy(
                quantity = existing.quantity + normalizedEntry.quantity,
            )
        }
    }

    return merged
}

private fun CollectionCardEntry.mergeIdentity(): MergeIdentity {
    return MergeIdentity(
        cardKey = card.id.ifBlank { card.name.trim().lowercase() },
        cardName = card.name.trim().lowercase(),
        foil = foil,
        language = language,
        condition = condition,
        price = price,
        artLabel = artLabel.trim().lowercase(),
        artImageUrl = artImageUrl?.trim().orEmpty(),
    )
}

private data class MergeIdentity(
    val cardKey: String,
    val cardName: String,
    val foil: FoilOption,
    val language: LanguageOption,
    val condition: CardCondition,
    val price: Double?,
    val artLabel: String,
    val artImageUrl: String,
)
