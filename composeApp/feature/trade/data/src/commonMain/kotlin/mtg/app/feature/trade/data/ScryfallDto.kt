package mtg.app.feature.trade.data

import mtg.app.feature.trade.domain.MtgCard
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

fun JsonObject.toCardOrNull(): MtgCard? {
    val id = getString("id") ?: return null
    val name = getString("name") ?: return null
    val typeLine = getString("type_line") ?: ""
    val imageUrl = getObject("image_uris")?.getString("normal")
        ?: getFirstFaceImageUrl()
    val artDescriptor = buildArtDescriptor()

    return MtgCard(
        id = id,
        name = name,
        typeLine = typeLine,
        imageUrl = imageUrl,
        artDescriptor = artDescriptor,
    )
}

fun JsonObject.toCards(): List<MtgCard> {
    val data = this["data"] as? JsonArray ?: return emptyList()

    return data.mapNotNull { element ->
        val card = element as? JsonObject ?: return@mapNotNull null
        card.toCardOrNull()
    }
}

fun JsonObject.toDefaultCardsBulkUrl(): String? {
    val data = this["data"] as? JsonArray ?: return null

    return data.firstNotNullOfOrNull { element ->
        val item = element as? JsonObject ?: return@firstNotNullOfOrNull null
        val type = item.getString("type")
        val downloadUri = item.getString("download_uri")

        if (type == "default_cards") downloadUri else null
    }
}

fun JsonObject.toImportCardsBulkUrl(): String? {
    val data = this["data"] as? JsonArray ?: return null

    var defaultCardsUrl: String? = null
    data.forEach { element ->
        val item = element as? JsonObject ?: return@forEach
        val type = item.getString("type")
        val downloadUri = item.getString("download_uri")
        when (type) {
            "oracle_cards" -> return downloadUri
            "default_cards" -> if (defaultCardsUrl == null) defaultCardsUrl = downloadUri
        }
    }
    return defaultCardsUrl
}

private fun JsonObject.getString(key: String): String? {
    val primitive = this[key] as? JsonPrimitive ?: return null
    return runCatching { primitive.content }.getOrNull()
}

private fun JsonObject.getObject(key: String): JsonObject? {
    return this[key] as? JsonObject
}

private fun JsonObject.getFirstFaceImageUrl(): String? {
    val faces = this["card_faces"] as? JsonArray ?: return null
    return faces.firstNotNullOfOrNull { face ->
        (face as? JsonObject)?.getObject("image_uris")?.getString("normal")
    }
}

private fun JsonObject.getBoolean(key: String): Boolean {
    val primitive = this[key] as? JsonPrimitive ?: return false
    val content = runCatching { primitive.content }.getOrNull() ?: return false
    return content.toBooleanStrictOrNull() ?: false
}

private fun JsonObject.getStringArray(key: String): List<String> {
    val array = this[key] as? JsonArray ?: return emptyList()
    return array.mapNotNull { element ->
        val primitive = element as? JsonPrimitive ?: return@mapNotNull null
        runCatching { primitive.content }.getOrNull()
    }
}

private fun JsonObject.buildArtDescriptor(): String {
    val frameEffects = getStringArray("frame_effects")
    val tags = mutableListOf<String>()

    if ("showcase" in frameEffects) tags += "Showcase"
    if ("extendedart" in frameEffects) tags += "Extended Art"
    if ("borderless" in frameEffects) tags += "Borderless"
    if ("etched" in frameEffects) tags += "Etched"
    if (getBoolean("promo")) tags += "Promo"
    if (getBoolean("variation")) tags += "Variant"
    if (tags.isEmpty()) tags += "Regular"

    val setCode = getString("set")?.uppercase()
    val collectorNumber = getString("collector_number")
    val version = listOfNotNull(
        setCode,
        collectorNumber?.let { "#$it" },
    ).joinToString(" ")

    return if (version.isBlank()) {
        tags.joinToString(", ")
    } else {
        "${tags.joinToString(", ")} ($version)"
    }
}
