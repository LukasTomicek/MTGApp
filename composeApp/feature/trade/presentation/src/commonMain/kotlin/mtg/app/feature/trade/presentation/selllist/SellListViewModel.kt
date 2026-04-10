package mtg.app.feature.trade.presentation.selllist

import mtg.app.core.presentation.BaseViewModel
import mtg.app.feature.auth.domain.AuthDomainService
import mtg.app.feature.trade.domain.MtgCard
import mtg.app.feature.trade.domain.StoredMapPin
import mtg.app.feature.trade.domain.TradeFilter
import mtg.app.feature.trade.domain.TradeListType
import mtg.app.feature.trade.domain.TradeService
import mtg.app.core.domain.obj.AuthContext
import mtg.app.feature.trade.presentation.utils.model.CardCondition
import mtg.app.feature.trade.presentation.utils.model.CollectionArtOption
import mtg.app.feature.trade.presentation.utils.model.CollectionCardEntry
import mtg.app.feature.trade.presentation.utils.model.FoilOption
import mtg.app.feature.trade.presentation.utils.model.LanguageOption
import mtg.app.core.presentation.utils.formatEuroPrice
import mtg.app.feature.trade.presentation.utils.model.hasSameMergeIdentityAs
import mtg.app.feature.trade.presentation.utils.model.mergeDuplicateEntries
import mtg.app.feature.trade.presentation.utils.mapper.toCollectionCardEntry
import mtg.app.feature.trade.presentation.utils.mapper.toStoredTradeCardEntry
import mtg.app.feature.trade.presentation.utils.SellListTransferStore
import kotlinx.coroutines.Job

class SellListViewModel(
    private val tradeService: TradeService,
    private val sellListTransferStore: SellListTransferStore,
    private val authService: AuthDomainService,
) : BaseViewModel<SellListScreenState, SellListUiEvent, SellListDirection>(
    initialState = SellListScreenState(),
) {
    private var nextEntryNumber: Int = 1
    private var currentUid: String? = null
    private var currentIdToken: String? = null
    private var currentUserEmail: String? = null
    private var hasMapPins: Boolean? = null
    private var persistJob: Job? = null
    private var lastPersistedSignature: String? = null

    init {
        launch {
            authService.currentUser.collect { user ->
                currentUid = user?.uid
                currentIdToken = user?.idToken
                currentUserEmail = user?.email
                if (user == null) {
                    hasMapPins = null
                    updateState {
                        it.copy(
                            collectionEntries = emptyList(),
                            visibleCollectionEntries = emptyList(),
                            selectedCollectionEntryId = null,
                            infoMessage = "Sign in to sync your sell list",
                        )
                    }
                } else {
                    loadPersistedEntries(uid = user.uid, idToken = user.idToken)
                    refreshMapPinsPresence(uid = user.uid, idToken = user.idToken)
                }
            }
        }

        launch {
            sellListTransferStore.pendingEntries.collect { pending ->
                if (pending.isEmpty()) return@collect

                val before = state.value.data.collectionEntries
                updateState { state ->
                    val pendingEntries = pending.map { incoming ->
                        incoming.copy(entryId = "entry-${nextEntryNumber++}")
                    }
                    val updatedEntries = mergeDuplicateEntries(state.collectionEntries + pendingEntries)
                    val filtered = filterCollection(updatedEntries, state.collectionSearchQuery)
                    val selectedId = pendingEntries.lastOrNull()?.let { added ->
                        updatedEntries.firstOrNull { it.hasSameMergeIdentityAs(added) }?.entryId
                    } ?: filtered.lastOrNull()?.entryId
                    state.copy(
                        collectionEntries = updatedEntries,
                        visibleCollectionEntries = filtered,
                        selectedCollectionEntryId = selectedId,
                        infoMessage = "Added ${pending.size} item(s) from Collection",
                    )
                }
                sellListTransferStore.markConsumed(pending.size)
                syncEntriesIfChanged(before)
            }
        }
    }

    override fun onUiEvent(event: SellListUiEvent) {
        when (event) {
            is SellListUiEvent.CollectionSearchChanged -> {
                updateState { state ->
                    val filtered = filterCollection(state.collectionEntries, event.value)
                    state.copy(
                        collectionSearchQuery = event.value,
                        visibleCollectionEntries = filtered,
                        selectedCollectionEntryId = filtered.firstOrNull()?.entryId,
                    )
                }
            }

            is SellListUiEvent.CollectionCardClicked -> {
                updateState { it.copy(selectedCollectionEntryId = event.entryId) }
            }

            SellListUiEvent.EnterAddModeClicked -> {
                val seedQuery = state.value.data.collectionSearchQuery.trim()
                updateState {
                    it.copy(
                        isAddMode = true,
                        editingEntryId = null,
                        addSearchQuery = seedQuery,
                        addResults = emptyList(),
                        selectedAddCard = null,
                        addQuantityInput = "1",
                        addFoil = FoilOption.NON_FOIL,
                        addLanguage = LanguageOption.EN,
                        addCondition = CardCondition.NM,
                        addPriceInput = "",
                        addArtOptions = emptyList(),
                        selectedAddArtId = "",
                        infoMessage = "",
                        showPinRecommendationDialog = hasMapPins == false,
                    )
                }
                ensureMapPinsLoadedAndPromptIfMissing()

                if (seedQuery.isNotBlank()) {
                    loadAddResults()
                }
            }

            SellListUiEvent.ExitAddModeClicked -> {
                updateState {
                    it.copy(
                        isAddMode = false,
                        editingEntryId = null,
                        selectedAddCard = null,
                        showPinRecommendationDialog = false,
                    )
                }
            }

            is SellListUiEvent.EditEntryClicked -> {
                var entryCard: MtgCard? = null
                var entryArtImageUrl: String? = null

                updateState { state ->
                    val entry = state.collectionEntries.firstOrNull { it.entryId == event.entryId }
                        ?: return@updateState state.copy(infoMessage = "Entry not found")

                    entryCard = entry.card
                    entryArtImageUrl = entry.artImageUrl

                    state.copy(
                        isAddMode = false,
                        editingEntryId = entry.entryId,
                        addSearchQuery = entry.card.name,
                        addResults = emptyList(),
                        selectedAddCard = entry.card,
                        addQuantityInput = entry.quantity.toString(),
                        addFoil = entry.foil,
                        addLanguage = entry.language,
                        addCondition = entry.condition,
                        addPriceInput = entry.price?.toString().orEmpty(),
                        addArtOptions = emptyList(),
                        selectedAddArtId = "",
                        infoMessage = "Edit parameters and save",
                    )
                }

                val card = entryCard
                if (card != null) {
                    loadArtOptionsForCard(card, preferredImageUrl = entryArtImageUrl)
                }
            }

            is SellListUiEvent.RemoveEntryClicked -> {
                val before = state.value.data.collectionEntries
                updateState { state ->
                    val updatedEntries = state.collectionEntries.filterNot { it.entryId == event.entryId }
                    val filtered = filterCollection(updatedEntries, state.collectionSearchQuery)

                    state.copy(
                        collectionEntries = updatedEntries,
                        visibleCollectionEntries = filtered,
                        selectedCollectionEntryId = filtered.firstOrNull()?.entryId,
                        infoMessage = "Item removed",
                    )
                }
                syncEntriesIfChanged(before)
            }

            is SellListUiEvent.AddSearchChanged -> {
                updateState { it.copy(addSearchQuery = event.value) }
            }

            SellListUiEvent.AddSearchSubmitted -> {
                loadAddResults()
            }

            is SellListUiEvent.AddResultClicked -> {
                var selectedCard: MtgCard? = null

                updateState { state ->
                    val card = state.addResults.firstOrNull { it.id == event.cardId }
                    if (card == null) {
                        state.copy(infoMessage = "Card not found")
                    } else {
                        selectedCard = card
                        state.copy(
                            selectedAddCard = card,
                            addArtOptions = emptyList(),
                            selectedAddArtId = "",
                            infoMessage = "Loading card art variants",
                        )
                    }
                }

                selectedCard?.let { loadArtOptionsForCard(it) }
            }

            is SellListUiEvent.QuantityChanged -> {
                updateState { it.copy(addQuantityInput = event.value.filter(Char::isDigit)) }
            }

            is SellListUiEvent.FoilSelected -> {
                updateState { it.copy(addFoil = event.value) }
            }

            is SellListUiEvent.LanguageSelected -> {
                updateState { it.copy(addLanguage = event.value) }
            }

            is SellListUiEvent.ConditionSelected -> {
                updateState { it.copy(addCondition = event.value) }
            }

            is SellListUiEvent.PriceChanged -> {
                updateState { it.copy(addPriceInput = sanitizePriceInput(event.value)) }
            }

            is SellListUiEvent.ArtSelected -> {
                updateState { it.copy(selectedAddArtId = event.artId) }
            }

            SellListUiEvent.DismissAddCardParametersClicked -> {
                updateState {
                    it.copy(
                        editingEntryId = null,
                        selectedAddCard = null,
                        addArtOptions = emptyList(),
                        selectedAddArtId = "",
                    )
                }
            }

            SellListUiEvent.ConfirmAddClicked -> {
                val before = state.value.data.collectionEntries
                updateState { state ->
                    val card = state.selectedAddCard
                        ?: return@updateState state.copy(infoMessage = "Select card in add results")
                    val quantity = state.addQuantityInput.toIntOrNull()
                        ?: return@updateState state.copy(infoMessage = "Invalid quantity")
                    if (quantity <= 0) {
                        return@updateState state.copy(infoMessage = "Quantity must be > 0")
                    }
                    val rawPrice = state.addPriceInput.trim()
                    val price = parsePriceOrNull(rawPrice)
                    if (rawPrice.isNotBlank() && price == null) {
                        return@updateState state.copy(infoMessage = "Invalid price")
                    }
                    if (price != null && price <= 0.0) {
                        return@updateState state.copy(infoMessage = "Price must be > 0")
                    }

                    val selectedArt = state.addArtOptions.firstOrNull { it.id == state.selectedAddArtId }
                        ?: fallbackArtOption(card)
                    val cardWithSelectedArt = card.copy(imageUrl = selectedArt.imageUrl ?: card.imageUrl)

                    val submittedEntry = CollectionCardEntry(
                        entryId = state.editingEntryId ?: "entry-${nextEntryNumber++}",
                        card = cardWithSelectedArt,
                        quantity = quantity,
                        foil = state.addFoil,
                        language = state.addLanguage,
                        condition = state.addCondition,
                        price = price,
                        artLabel = selectedArt.label,
                        artImageUrl = selectedArt.imageUrl,
                    )
                    val withSubmitted = if (state.editingEntryId == null) {
                        state.collectionEntries + submittedEntry
                    } else {
                        state.collectionEntries.map { entry ->
                            if (entry.entryId == state.editingEntryId) submittedEntry else entry
                        }
                    }
                    val updatedEntries = mergeDuplicateEntries(withSubmitted)

                    val filtered = filterCollection(updatedEntries, state.collectionSearchQuery)
                    val selectedId = updatedEntries.firstOrNull {
                        it.hasSameMergeIdentityAs(submittedEntry)
                    }?.entryId ?: updatedEntries.lastOrNull()?.entryId

                    state.copy(
                        isAddMode = false,
                        editingEntryId = null,
                        collectionSearchQuery = "",
                        collectionEntries = updatedEntries,
                        visibleCollectionEntries = if (state.editingEntryId == null) {
                            updatedEntries
                        } else {
                            filtered
                        },
                        selectedCollectionEntryId = selectedId,
                        selectedAddCard = null,
                        addArtOptions = emptyList(),
                        selectedAddArtId = "",
                        addPriceInput = "",
                        infoMessage = if (state.editingEntryId == null) {
                            "Added ${card.name} to collection"
                        } else {
                            "Updated ${card.name}"
                        },
                    )
                }
                syncEntriesIfChanged(before)
            }

            SellListUiEvent.DismissPinRecommendationClicked -> {
                updateState { it.copy(showPinRecommendationDialog = false) }
            }

            is SellListUiEvent.CurrentLocationResolved -> {
                savePinFromCurrentLocation(event.coordinate)
            }

            SellListUiEvent.ImportSampleClicked -> {
                val before = state.value.data.collectionEntries
                updateState { state ->
                    val sample = listOf(
                        CollectionCardEntry(
                            entryId = "entry-${nextEntryNumber++}",
                            card = sampleCard("sol-ring", "Sol Ring", "Artifact"),
                            quantity = 1,
                            foil = FoilOption.NON_FOIL,
                            language = LanguageOption.EN,
                            condition = CardCondition.NM,
                            price = 5.0,
                            artLabel = "Default art",
                            artImageUrl = null,
                        ),
                        CollectionCardEntry(
                            entryId = "entry-${nextEntryNumber++}",
                            card = sampleCard("lightning-bolt", "Lightning Bolt", "Instant"),
                            quantity = 2,
                            foil = FoilOption.FOIL,
                            language = LanguageOption.EN,
                            condition = CardCondition.EXC,
                            price = 2.0,
                            artLabel = "Default art",
                            artImageUrl = null,
                        ),
                    )

                    val merged = mergeDuplicateEntries(state.collectionEntries + sample)
                    val filtered = filterCollection(merged, state.collectionSearchQuery)

                    state.copy(
                        collectionEntries = merged,
                        visibleCollectionEntries = filtered,
                        selectedCollectionEntryId = filtered.firstOrNull()?.entryId,
                        infoMessage = "Imported sample collection",
                    )
                }
                syncEntriesIfChanged(before)
            }

            SellListUiEvent.ExportClicked -> {
                updateState { state ->
                    val text = if (state.collectionEntries.isEmpty()) {
                        "Collection is empty"
                    } else {
                        state.collectionEntries.joinToString("\n") { entry ->
                            "${entry.quantity}x ${entry.card.name} | ${entry.foil.label} | ${entry.language.label} | ${entry.condition.label} | ${entry.artLabel} | ${formatPrice(entry.price)}"
                        }
                    }

                    state.copy(
                        exportPreview = text,
                        infoMessage = "Export generated",
                    )
                }
            }
        }
    }

    private fun loadAddResults() {
        launch {
            setLoading(true)
            setError(null)

            try {
                val query = state.value.data.addSearchQuery.trim()
                val cards = if (query.isBlank()) {
                    emptyList()
                } else {
                    tradeService.searchCards(query = query, filter = TradeFilter.ALL)
                }

                updateState {
                    it.copy(
                        addResults = cards,
                        selectedAddCard = if (it.editingEntryId == null) null else it.selectedAddCard,
                        addArtOptions = if (it.editingEntryId == null) emptyList() else it.addArtOptions,
                        selectedAddArtId = if (it.editingEntryId == null) "" else it.selectedAddArtId,
                        infoMessage = if (query.isBlank()) "Type card name to search" else "Found ${cards.size} cards",
                    )
                }
            } catch (e: Throwable) {
                setError(e.message ?: "Failed to load cards")
                updateState { it.copy(addResults = emptyList(), addArtOptions = emptyList(), selectedAddArtId = "") }
            } finally {
                setLoading(false)
            }
        }
    }

    private fun loadArtOptionsForCard(card: MtgCard, preferredImageUrl: String? = null) {
        launch {
            setLoading(true)
            setError(null)

            try {
                val prints = tradeService.searchCardPrints(cardName = card.name)
                val options = buildArtOptions(card = card, prints = prints)
                val selectedId = resolveSelectedArtId(
                    options = options,
                    preferredImageUrl = preferredImageUrl,
                )

                updateState { state ->
                    if (state.selectedAddCard?.id != card.id) {
                        state
                    } else {
                        state.copy(
                            addArtOptions = options,
                            selectedAddArtId = selectedId,
                            infoMessage = "Choose card parameters",
                        )
                    }
                }
            } catch (_: Throwable) {
                val fallback = listOf(fallbackArtOption(card))
                updateState { state ->
                    if (state.selectedAddCard?.id != card.id) {
                        state
                    } else {
                        state.copy(
                            addArtOptions = fallback,
                            selectedAddArtId = fallback.first().id,
                            infoMessage = "Choose card parameters",
                        )
                    }
                }
            } finally {
                setLoading(false)
            }
        }
    }

    private fun syncEntriesIfChanged(before: List<CollectionCardEntry>) {
        val after = state.value.data.collectionEntries
        if (after != before) {
            persistEntries()
        }
    }

    private fun persistEntries() {
        val uid = currentUid ?: return
        val idToken = currentIdToken ?: return
        val entries = state.value.data.collectionEntries.map { it.toStoredTradeCardEntry() }
        val signature = entriesSignature(state.value.data.collectionEntries)
        if (signature == lastPersistedSignature) {
            println("TradeBE: sell list persist skipped (unchanged)")
            return
        }

        persistJob?.cancel()
        persistJob = domainCall(
            loading = null,
            clearErrorOnStart = false,
            onError = { throwable ->
                setError(throwable.message ?: "Failed to sync sell list")
            },
            action = {
                tradeService.replaceListEntries(
                    context = AuthContext(uid = uid, idToken = idToken),
                    listType = TradeListType.SELL_LIST,
                    entries = entries,
                    actorEmail = currentUserEmail,
                )
                lastPersistedSignature = signature
            },
        )
    }

    private fun loadPersistedEntries(uid: String, idToken: String) {
        domainCall(
            action = {
                val persistedEntries = tradeService.loadListEntries(
                    context = AuthContext(uid = uid, idToken = idToken),
                    listType = TradeListType.SELL_LIST,
                ).map { it.toCollectionCardEntry() }
                val pending = sellListTransferStore.pendingEntries.value
                var nextGeneratedId = nextEntryNumber(persistedEntries)
                val pendingWithGeneratedIds = pending.map { incoming ->
                    incoming.copy(entryId = "entry-${nextGeneratedId++}")
                }
                val entries = mergeDuplicateEntries(persistedEntries + pendingWithGeneratedIds)
                if (pendingWithGeneratedIds.isNotEmpty()) {
                    sellListTransferStore.markConsumed(pendingWithGeneratedIds.size)
                }
                SellLoadSnapshot(entries, pendingWithGeneratedIds.size)
            },
            onError = { throwable ->
                setError(throwable.message ?: "Failed to load sell list")
            },
        ) { snapshot ->
            nextEntryNumber = nextEntryNumber(snapshot.entries)
            lastPersistedSignature = entriesSignature(snapshot.entries)
            updateState {
                it.copy(
                    collectionEntries = snapshot.entries,
                    visibleCollectionEntries = snapshot.entries,
                    selectedCollectionEntryId = snapshot.entries.firstOrNull()?.entryId,
                    infoMessage = if (snapshot.entries.isEmpty()) {
                        "Sell list is empty"
                    } else if (snapshot.transferredCount > 0) {
                        "Loaded ${snapshot.entries.size} items (${snapshot.transferredCount} from Collection)"
                    } else {
                        "Loaded ${snapshot.entries.size} items"
                    },
                )
            }
        }
    }

    private fun refreshMapPinsPresence(uid: String, idToken: String) {
        domainCall(
            loading = null,
            clearErrorOnStart = false,
            onError = { hasMapPins = null },
            action = {
                tradeService.loadMapPins(context = AuthContext(uid = uid, idToken = idToken))
            },
        ) { pins ->
                hasMapPins = pins.isNotEmpty()
        }
    }

    private fun ensureMapPinsLoadedAndPromptIfMissing() {
        if (hasMapPins != null) return
        val uid = currentUid ?: return
        val idToken = currentIdToken ?: return
        domainCall(
            loading = null,
            clearErrorOnStart = false,
            onError = { hasMapPins = null },
            action = {
                tradeService.loadMapPins(context = AuthContext(uid = uid, idToken = idToken))
            },
        ) { pins ->
                hasMapPins = pins.isNotEmpty()
                if (pins.isEmpty() && state.value.data.isAddMode) {
                    updateState { it.copy(showPinRecommendationDialog = true) }
                }
        }
    }

    private fun savePinFromCurrentLocation(coordinate: mtg.app.feature.map.presentation.map.MapCoordinate?) {
        val uid = currentUid
        val idToken = currentIdToken
        if (uid == null || idToken == null) {
            updateState {
                it.copy(
                    showPinRecommendationDialog = false,
                    infoMessage = "Sign in required",
                )
            }
            return
        }

        if (coordinate == null) {
            updateState {
                it.copy(
                    showPinRecommendationDialog = false,
                    infoMessage = "Could not get current location",
                )
            }
            return
        }

        domainCall(
            loading = null,
            clearErrorOnStart = false,
            onError = { throwable ->
                updateState {
                    it.copy(
                        showPinRecommendationDialog = false,
                        infoMessage = throwable.message ?: "Failed to save map pin",
                    )
                }
            },
            action = {
                val existing = tradeService.loadMapPins(context = AuthContext(uid = uid, idToken = idToken))
                val nextPin = StoredMapPin(
                    pinId = "pin-${nextMapPinNumber(existing) + 1}",
                    latitude = coordinate.latitude,
                    longitude = coordinate.longitude,
                    radiusMeters = if (existing.isEmpty()) 1_000f else existing.first().radiusMeters,
                )
                tradeService.replaceMapPins(
                    context = AuthContext(uid = uid, idToken = idToken),
                    pins = existing + nextPin,
                    actorEmail = currentUserEmail,
                    triggerRematch = true,
                )
            },
        ) {
                hasMapPins = true
                updateState {
                    it.copy(
                        showPinRecommendationDialog = false,
                        infoMessage = "Current location saved as map pin",
                    )
                }
        }
    }
}

private data class SellLoadSnapshot(
    val entries: List<CollectionCardEntry>,
    val transferredCount: Int,
)

private fun filterCollection(
    entries: List<CollectionCardEntry>,
    query: String,
): List<CollectionCardEntry> {
    if (query.isBlank()) return entries
    val needle = query.trim()
    return entries.filter { it.card.name.contains(needle, ignoreCase = true) }
}

private fun sampleCard(id: String, name: String, type: String): MtgCard {
    return MtgCard(
        id = id,
        name = name,
        typeLine = type,
        imageUrl = null,
    )
}

private fun buildArtOptions(card: MtgCard, prints: List<MtgCard>): List<CollectionArtOption> {
    val uniquePrints = prints
        .asSequence()
        .filter { !it.imageUrl.isNullOrBlank() }
        .distinctBy { it.imageUrl }
        .toList()

    if (uniquePrints.isEmpty()) {
        return listOf(fallbackArtOption(card))
    }

    return uniquePrints.mapIndexed { index, print ->
        CollectionArtOption(
            id = print.id,
            label = print.artDescriptor ?: if (uniquePrints.size == 1) "Default art" else "Art ${index + 1}",
            imageUrl = print.imageUrl,
        )
    }
}

private fun resolveSelectedArtId(
    options: List<CollectionArtOption>,
    preferredImageUrl: String?,
): String {
    if (options.isEmpty()) return ""
    if (!preferredImageUrl.isNullOrBlank()) {
        val preferred = options.firstOrNull { it.imageUrl == preferredImageUrl }
        if (preferred != null) return preferred.id
    }
    return options.first().id
}

private fun fallbackArtOption(card: MtgCard): CollectionArtOption {
    return CollectionArtOption(
        id = "art-${card.id}-fallback",
        label = "Default art",
        imageUrl = card.imageUrl,
    )
}

private fun nextMapPinNumber(pins: List<StoredMapPin>): Int {
    return pins.mapNotNull { pin ->
        pin.pinId.removePrefix("pin-").toIntOrNull()
    }.maxOrNull() ?: 0
}

private fun nextEntryNumber(entries: List<CollectionCardEntry>): Int {
    val maxId = entries.mapNotNull { entry ->
        entry.entryId.removePrefix("entry-").toIntOrNull()
    }.maxOrNull() ?: 0
    return maxId + 1
}

private fun sanitizePriceInput(raw: String): String {
    val normalized = raw.replace(',', '.')
    val builder = StringBuilder()
    var seenDot = false
    normalized.forEach { c ->
        when {
            c.isDigit() -> builder.append(c)
            c == '.' && !seenDot -> {
                seenDot = true
                builder.append(c)
            }
        }
    }
    return builder.toString()
}

private fun parsePriceOrNull(raw: String): Double? {
    val value = raw.trim().replace(',', '.')
    if (value.isBlank()) return null
    return value.toDoubleOrNull()
}

private fun formatPrice(value: Double?): String {
    return formatEuroPrice(value)
}

private fun entriesSignature(entries: List<CollectionCardEntry>): String {
    return entries
        .sortedBy { it.entryId }
        .joinToString(separator = "||") { entry ->
            listOf(
                entry.entryId,
                entry.card.id,
                entry.card.name,
                entry.quantity.toString(),
                entry.foil.name,
                entry.language.name,
                entry.condition.name,
                entry.price?.toString().orEmpty(),
                entry.artLabel,
                entry.artImageUrl.orEmpty(),
            ).joinToString("|")
        }
}
