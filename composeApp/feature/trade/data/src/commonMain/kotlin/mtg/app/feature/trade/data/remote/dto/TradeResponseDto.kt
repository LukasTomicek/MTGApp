package mtg.app.feature.trade.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import mtg.app.feature.trade.domain.MarketPlaceCard
import mtg.app.feature.trade.domain.MarketPlaceSeller
import mtg.app.feature.trade.domain.StoredMapPin
import mtg.app.feature.trade.domain.StoredTradeCardEntry

@Serializable
data class EnsureChatResponseDto(
    @SerialName("chatId")
    val chatId: String = "",
)

@Serializable
data class OfferResponseDto(
    @SerialName("id")
    val id: String = "",
    @SerialName("cardId")
    val cardId: String = "",
    @SerialName("cardName")
    val cardName: String = "",
    @SerialName("cardTypeLine")
    val cardTypeLine: String? = null,
    @SerialName("typeLine")
    val typeLine: String? = null,
    @SerialName("cardImageUrl")
    val cardImageUrl: String? = null,
    @SerialName("imageUrl")
    val imageUrl: String? = null,
    @SerialName("price")
    val price: Double? = null,
)

@Serializable
data class CollectionEntryResponseDto(
    @SerialName("entryId")
    val entryId: String? = null,
    @SerialName("cardId")
    val cardId: String = "",
    @SerialName("cardName")
    val cardName: String = "",
    @SerialName("cardTypeLine")
    val cardTypeLine: String = "",
    @SerialName("cardImageUrl")
    val cardImageUrl: String? = null,
    @SerialName("cardArtDescriptor")
    val cardArtDescriptor: String? = null,
    @SerialName("quantity")
    val quantity: Int = 1,
    @SerialName("foil")
    val foil: String = "NON_FOIL",
    @SerialName("language")
    val language: String = "EN",
    @SerialName("condition")
    val condition: String = "NM",
    @SerialName("price")
    val price: Double? = null,
    @SerialName("artLabel")
    val artLabel: String = "Default art",
    @SerialName("artImageUrl")
    val artImageUrl: String? = null,
)

@Serializable
data class MarketCardResponseDto(
    @SerialName("cardId")
    val cardId: String = "",
    @SerialName("cardName")
    val cardName: String = "",
    @SerialName("cardTypeLine")
    val cardTypeLine: String? = null,
    @SerialName("typeLine")
    val typeLine: String? = null,
    @SerialName("imageUrl")
    val imageUrl: String? = null,
    @SerialName("cardImageUrl")
    val cardImageUrl: String? = null,
    @SerialName("offerCount")
    val offerCount: Int = 0,
    @SerialName("fromPrice")
    val fromPrice: Double? = null,
)

@Serializable
data class MarketSellerResponseDto(
    @SerialName("userId")
    val userId: String = "",
    @SerialName("displayName")
    val displayName: String? = null,
    @SerialName("offerCount")
    val offerCount: Int = 0,
    @SerialName("fromPrice")
    val fromPrice: Double? = null,
)

internal fun Map<String, CollectionEntryResponseDto>.toStoredTradeEntries(): List<StoredTradeCardEntry> {
    return entries.mapNotNull { (fallbackEntryId, dto) ->
        val resolvedEntryId = dto.entryId?.trim().takeUnless { it.isNullOrBlank() } ?: fallbackEntryId
        if (resolvedEntryId.isBlank()) return@mapNotNull null
        StoredTradeCardEntry(
            entryId = resolvedEntryId,
            cardId = dto.cardId.trim(),
            cardName = dto.cardName.trim(),
            cardTypeLine = dto.cardTypeLine.trim(),
            cardImageUrl = dto.cardImageUrl?.trim()?.takeUnless { it.isBlank() },
            cardArtDescriptor = dto.cardArtDescriptor?.trim()?.takeUnless { it.isBlank() },
            quantity = dto.quantity,
            foil = dto.foil,
            language = dto.language,
            condition = dto.condition,
            price = dto.price,
            artLabel = dto.artLabel,
            artImageUrl = dto.artImageUrl?.trim()?.takeUnless { it.isBlank() },
        )
    }
}

internal fun List<OfferResponseDto>.toStoredTradeEntries(): List<StoredTradeCardEntry> {
    return mapNotNull { dto ->
        val offerId = dto.id.trim()
        val cardName = dto.cardName.trim()
        if (offerId.isBlank() || cardName.isBlank()) return@mapNotNull null
        StoredTradeCardEntry(
            entryId = offerId,
            cardId = dto.cardId.trim(),
            cardName = cardName,
            cardTypeLine = dto.cardTypeLine?.trim()?.takeUnless { it.isBlank() }
                ?: dto.typeLine?.trim()?.takeUnless { it.isBlank() }
                ?: "",
            cardImageUrl = dto.cardImageUrl?.trim()?.takeUnless { it.isBlank() }
                ?: dto.imageUrl?.trim()?.takeUnless { it.isBlank() },
            cardArtDescriptor = null,
            quantity = 1,
            foil = "NON_FOIL",
            language = "EN",
            condition = "NM",
            price = dto.price,
            artLabel = "Default art",
            artImageUrl = null,
        )
    }
}

internal fun Map<String, MapPinDto>.toStoredMapPins(): List<StoredMapPin> {
    return entries.map { (fallbackPinId, dto) ->
        StoredMapPin(
            pinId = dto.pinId.trim().ifBlank { fallbackPinId },
            latitude = dto.latitude,
            longitude = dto.longitude,
            radiusMeters = dto.radiusMeters,
        )
    }
}

internal fun List<MarketCardResponseDto>.toMarketPlaceCards(): List<MarketPlaceCard> {
    return mapNotNull { dto ->
        val cardId = dto.cardId.trim()
        val cardName = dto.cardName.trim()
        if (cardId.isBlank() || cardName.isBlank()) return@mapNotNull null
        MarketPlaceCard(
            cardId = cardId,
            cardName = cardName,
            cardTypeLine = dto.cardTypeLine?.trim()?.takeUnless { it.isBlank() }
                ?: dto.typeLine?.trim()?.takeUnless { it.isBlank() }
                ?: "",
            imageUrl = dto.imageUrl?.trim()?.takeUnless { it.isBlank() }
                ?: dto.cardImageUrl?.trim()?.takeUnless { it.isBlank() },
            offerCount = dto.offerCount,
            fromPrice = dto.fromPrice,
        )
    }
}

internal fun List<MarketSellerResponseDto>.toMarketPlaceSellers(): List<MarketPlaceSeller> {
    return mapNotNull { dto ->
        val uid = dto.userId.trim()
        if (uid.isBlank()) return@mapNotNull null
        MarketPlaceSeller(
            uid = uid,
            displayName = dto.displayName?.trim().takeUnless { it.isNullOrBlank() } ?: uid,
            offerCount = dto.offerCount,
            fromPrice = dto.fromPrice,
        )
    }.sortedBy { it.fromPrice ?: Double.MAX_VALUE }
}
