package mtg.app.feature.trade.data

import mtg.app.feature.trade.domain.StoredTradeCardEntry
import mtg.app.feature.trade.domain.StoredMapPin
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

fun JsonObject.toStoredTradeEntries(): List<StoredTradeCardEntry> {
    return this.entries.mapNotNull { (entryKey, element) ->
        val entryObject = element as? JsonObject ?: return@mapNotNull null
        entryObject.toStoredTradeEntry(fallbackEntryId = entryKey).takeIf { it.entryId.isNotBlank() }
    }
}

fun JsonObject.toStoredMapPins(): List<StoredMapPin> {
    return entries.mapNotNull { (pinKey, element) ->
        val pinObject = element as? JsonObject ?: return@mapNotNull null
        val latitude = pinObject.getDouble("latitude") ?: return@mapNotNull null
        val longitude = pinObject.getDouble("longitude") ?: return@mapNotNull null
        val radiusMeters = pinObject.getFloat("radiusMeters") ?: return@mapNotNull null
        StoredMapPin(
            pinId = pinObject.getString("pinId") ?: pinKey,
            latitude = latitude,
            longitude = longitude,
            radiusMeters = radiusMeters,
        )
    }
}

fun StoredTradeCardEntry.toRealtimeEntryJson(): JsonObject {
    return buildJsonObject {
        put("entryId", entryId)
        put("cardId", cardId)
        put("cardName", cardName)
        put("cardTypeLine", cardTypeLine)
        put("quantity", quantity)
        put("foil", foil)
        put("language", language)
        put("condition", condition)
        price?.let { put("price", it) }
        put("artLabel", artLabel)
        cardImageUrl?.let { put("cardImageUrl", it) }
        cardArtDescriptor?.let { put("cardArtDescriptor", it) }
        artImageUrl?.let { put("artImageUrl", it) }
    }
}

fun StoredMapPin.toRealtimeMapPinJson(): JsonObject {
    return buildJsonObject {
        put("pinId", pinId)
        put("latitude", latitude)
        put("longitude", longitude)
        put("radiusMeters", radiusMeters)
    }
}

private fun JsonObject.toStoredTradeEntry(fallbackEntryId: String? = null): StoredTradeCardEntry {
    val entryId = getString("entryId") ?: fallbackEntryId ?: return StoredTradeCardEntry(
        entryId = "",
        cardId = "",
        cardName = "",
        cardTypeLine = "",
        cardImageUrl = null,
        cardArtDescriptor = null,
        quantity = 1,
        foil = "NON_FOIL",
        language = "EN",
        condition = "NM",
        price = null,
        artLabel = "Default art",
        artImageUrl = null,
    )
    return StoredTradeCardEntry(
        entryId = entryId,
        cardId = getString("cardId") ?: "",
        cardName = getString("cardName") ?: "",
        cardTypeLine = getString("cardTypeLine") ?: "",
        cardImageUrl = getString("cardImageUrl"),
        cardArtDescriptor = getString("cardArtDescriptor"),
        quantity = getInt("quantity") ?: 1,
        foil = getString("foil") ?: "NON_FOIL",
        language = getString("language") ?: "EN",
        condition = getString("condition") ?: "NM",
        price = getDouble("price"),
        artLabel = getString("artLabel") ?: "Default art",
        artImageUrl = getString("artImageUrl"),
    )
}

private fun JsonObject.getString(key: String): String? {
    val primitive = this[key] as? JsonPrimitive ?: return null
    return runCatching { primitive.content }.getOrNull()
}

private fun JsonObject.getInt(key: String): Int? {
    val primitive = this[key] as? JsonPrimitive ?: return null
    val raw = runCatching { primitive.content }.getOrNull() ?: return null
    return raw.toIntOrNull()
}

private fun JsonObject.getDouble(key: String): Double? {
    val primitive = this[key] as? JsonPrimitive ?: return null
    val raw = runCatching { primitive.content }.getOrNull() ?: return null
    return raw.toDoubleOrNull()
}

private fun JsonObject.getFloat(key: String): Float? {
    val primitive = this[key] as? JsonPrimitive ?: return null
    val raw = runCatching { primitive.content }.getOrNull() ?: return null
    return raw.toFloatOrNull()
}
