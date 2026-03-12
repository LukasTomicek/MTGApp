package mtg.app.feature.trade.data.remote.dto

import mtg.app.feature.trade.domain.StoredMapPin
import mtg.app.feature.trade.domain.StoredTradeCardEntry
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EnsureChatRequestDto(
    @SerialName("buyerUid")
    val buyerUid: String,
    @SerialName("buyerEmail")
    val buyerEmail: String,
    @SerialName("sellerUid")
    val sellerUid: String,
    @SerialName("sellerEmail")
    val sellerEmail: String,
    @SerialName("cardId")
    val cardId: String,
    @SerialName("cardName")
    val cardName: String,
)

@Serializable
data class UpsertOfferRequestDto(
    @SerialName("cardId")
    val cardId: String,
    @SerialName("cardName")
    val cardName: String,
    @SerialName("type")
    val type: String,
    @SerialName("cardTypeLine")
    val cardTypeLine: String? = null,
    @SerialName("cardImageUrl")
    val cardImageUrl: String? = null,
    @SerialName("price")
    val price: Double? = null,
)

@Serializable
data class SyncOffersRequestDto(
    @SerialName("type")
    val type: String,
    @SerialName("entries")
    val entries: List<SyncOfferEntryDto>,
)

@Serializable
data class SyncOfferEntryDto(
    @SerialName("cardId")
    val cardId: String,
    @SerialName("cardName")
    val cardName: String,
    @SerialName("cardTypeLine")
    val cardTypeLine: String? = null,
    @SerialName("cardImageUrl")
    val cardImageUrl: String? = null,
    @SerialName("price")
    val price: Double? = null,
)

@Serializable
data class SyncMatchesRequestDto(
    @SerialName("type")
    val type: String,
)

@Serializable
data class MapPinDto(
    @SerialName("pinId")
    val pinId: String,
    @SerialName("latitude")
    val latitude: Double,
    @SerialName("longitude")
    val longitude: Double,
    @SerialName("radiusMeters")
    val radiusMeters: Float,
)

internal fun StoredTradeCardEntry.toUpsertOfferRequestDto(type: String): UpsertOfferRequestDto {
    val normalizedCardName = cardName.trim()
    val normalizedCardId = cardId.trim().ifBlank { normalizedCardName }
    val normalizedTypeLine = cardTypeLine.trim().takeIf { it.isNotBlank() }
    val normalizedImageUrl = artImageUrl?.trim()?.takeIf { it.isNotBlank() }
        ?: cardImageUrl?.trim()?.takeIf { it.isNotBlank() }

    return UpsertOfferRequestDto(
        cardId = normalizedCardId,
        cardName = normalizedCardName,
        type = type,
        cardTypeLine = normalizedTypeLine,
        cardImageUrl = normalizedImageUrl,
        price = price,
    )
}

internal fun StoredTradeCardEntry.toSyncOfferEntryDto(): SyncOfferEntryDto {
    val normalizedCardName = cardName.trim()
    val normalizedCardId = cardId.trim().ifBlank { normalizedCardName }
    val normalizedTypeLine = cardTypeLine.trim().takeIf { it.isNotBlank() }
    val normalizedImageUrl = artImageUrl?.trim()?.takeIf { it.isNotBlank() }
        ?: cardImageUrl?.trim()?.takeIf { it.isNotBlank() }

    return SyncOfferEntryDto(
        cardId = normalizedCardId,
        cardName = normalizedCardName,
        cardTypeLine = normalizedTypeLine,
        cardImageUrl = normalizedImageUrl,
        price = price,
    )
}

internal fun List<StoredMapPin>.toReplaceMapPinsRequestDto(): Map<String, MapPinDto> {
    return associate { pin ->
        pin.pinId to MapPinDto(
            pinId = pin.pinId,
            latitude = pin.latitude,
            longitude = pin.longitude,
            radiusMeters = pin.radiusMeters,
        )
    }
}
