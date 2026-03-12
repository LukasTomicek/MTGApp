package mtg.app.feature.trade.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import mtg.app.feature.trade.domain.MtgCard

@Serializable
data class ScryfallCollectionRequestDto(
    @SerialName("identifiers")
    val identifiers: List<ScryfallNameIdentifierDto>,
)

@Serializable
data class ScryfallNameIdentifierDto(
    @SerialName("name")
    val name: String,
)

@Serializable
data class ScryfallCardsResponseDto(
    @SerialName("data")
    val data: List<ScryfallCardDto> = emptyList(),
)

@Serializable
data class ScryfallBulkDataResponseDto(
    @SerialName("data")
    val data: List<ScryfallBulkItemDto> = emptyList(),
)

@Serializable
data class ScryfallBulkItemDto(
    @SerialName("type")
    val type: String? = null,
    @SerialName("download_uri")
    val downloadUri: String? = null,
)

@Serializable
data class ScryfallCardDto(
    @SerialName("id")
    val id: String? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("type_line")
    val typeLine: String? = null,
    @SerialName("image_uris")
    val imageUris: ScryfallImageUrisDto? = null,
    @SerialName("card_faces")
    val cardFaces: List<ScryfallCardFaceDto> = emptyList(),
    @SerialName("frame_effects")
    val frameEffects: List<String> = emptyList(),
    @SerialName("promo")
    val promo: Boolean = false,
    @SerialName("variation")
    val variation: Boolean = false,
    @SerialName("set")
    val set: String? = null,
    @SerialName("collector_number")
    val collectorNumber: String? = null,
)

@Serializable
data class ScryfallImageUrisDto(
    @SerialName("normal")
    val normal: String? = null,
)

@Serializable
data class ScryfallCardFaceDto(
    @SerialName("image_uris")
    val imageUris: ScryfallImageUrisDto? = null,
)

internal fun ScryfallCardsResponseDto.toCards(): List<MtgCard> {
    return data.mapNotNull { it.toCardOrNull() }
}

internal fun ScryfallBulkDataResponseDto.toDefaultCardsBulkUrl(): String? {
    return data.firstNotNullOfOrNull { item ->
        if (item.type == "default_cards") item.downloadUri else null
    }
}

private fun ScryfallCardDto.toCardOrNull(): MtgCard? {
    val resolvedId = id?.trim().takeUnless { it.isNullOrBlank() } ?: return null
    val resolvedName = name?.trim().takeUnless { it.isNullOrBlank() } ?: return null
    val resolvedTypeLine = typeLine?.trim().orEmpty()
    val imageUrl = imageUris?.normal?.trim()?.takeUnless { it.isBlank() }
        ?: cardFaces.firstNotNullOfOrNull { face ->
            face.imageUris?.normal?.trim()?.takeUnless { it.isBlank() }
        }
    return MtgCard(
        id = resolvedId,
        name = resolvedName,
        typeLine = resolvedTypeLine,
        imageUrl = imageUrl,
        artDescriptor = buildArtDescriptor(),
    )
}

private fun ScryfallCardDto.buildArtDescriptor(): String {
    val tags = mutableListOf<String>()
    if ("showcase" in frameEffects) tags += "Showcase"
    if ("extendedart" in frameEffects) tags += "Extended Art"
    if ("borderless" in frameEffects) tags += "Borderless"
    if ("etched" in frameEffects) tags += "Etched"
    if (promo) tags += "Promo"
    if (variation) tags += "Variant"
    if (tags.isEmpty()) tags += "Regular"

    val version = listOfNotNull(
        set?.uppercase(),
        collectorNumber?.trim()?.takeUnless { it.isBlank() }?.let { "#$it" },
    ).joinToString(" ")

    return if (version.isBlank()) tags.joinToString(", ") else "${tags.joinToString(", ")} ($version)"
}
