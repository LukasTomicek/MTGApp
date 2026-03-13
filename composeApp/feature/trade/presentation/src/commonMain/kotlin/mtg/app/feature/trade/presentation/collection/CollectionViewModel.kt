package mtg.app.feature.trade.presentation.collection

import mtg.app.core.presentation.BaseViewModel
import mtg.app.feature.auth.domain.AuthDomainService
import mtg.app.feature.trade.domain.MtgCard
import mtg.app.feature.trade.domain.TradeFilter
import mtg.app.feature.trade.domain.TradeListType
import mtg.app.feature.trade.domain.TradeService
import mtg.app.core.domain.obj.AuthContext
import mtg.app.feature.trade.presentation.utils.mapper.toCollectionCardEntry
import mtg.app.feature.trade.presentation.utils.mapper.toStoredTradeCardEntry
import mtg.app.feature.trade.presentation.utils.model.CardCondition
import mtg.app.feature.trade.presentation.utils.model.CollectionArtOption
import mtg.app.feature.trade.presentation.utils.model.CollectionCardEntry
import mtg.app.feature.trade.presentation.utils.model.FoilOption
import mtg.app.feature.trade.presentation.utils.model.LanguageOption
import mtg.app.feature.trade.presentation.utils.model.hasSameMergeIdentityAs
import mtg.app.feature.trade.presentation.utils.model.mergeDuplicateEntries
import mtg.app.feature.trade.presentation.utils.SellListTransferStore
import mtg.app.feature.trade.presentation.utils.scheduleCollectionBackgroundDelete
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield

class CollectionViewModel(
    private val tradeService: TradeService,
    private val sellListTransferStore: SellListTransferStore,
    private val authService: AuthDomainService,
) : BaseViewModel<CollectionScreenState, CollectionUiEvent, CollectionDirection>(
    initialState = CollectionScreenState(),
) {
    private var nextEntryNumber: Int = 1
    private var currentUid: String? = null
    private var currentIdToken: String? = null

    init {
        launch {
            authService.currentUser.collect { user ->
                currentUid = user?.uid
                currentIdToken = user?.idToken
                if (user == null) {
                    updateState {
                        it.copy(
                            collectionEntries = emptyList(),
                            visibleCollectionEntries = emptyList(),
                            selectedCollectionEntryId = null,
                            infoMessage = "Sign in to sync your collection",
                        )
                    }
                } else {
                    loadPersistedEntries(uid = user.uid, idToken = user.idToken)
                }
            }
        }
    }

    override fun onUiEvent(event: CollectionUiEvent) {
        when (event) {
            is CollectionUiEvent.CollectionSearchChanged -> {
                updateState { state ->
                    val filtered = filterCollection(state.collectionEntries, event.value)
                    state.copy(
                        collectionSearchQuery = event.value,
                        visibleCollectionEntries = filtered,
                        selectedCollectionEntryId = filtered.firstOrNull()?.entryId,
                    )
                }
            }

            is CollectionUiEvent.CollectionCardClicked -> {
                updateState { it.copy(selectedCollectionEntryId = event.entryId) }
            }

            CollectionUiEvent.EnterAddModeClicked -> {
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
                        addArtOptions = emptyList(),
                        selectedAddArtId = "",
                        infoMessage = "",
                    )
                }

                if (seedQuery.isNotBlank()) {
                    loadAddResults()
                }
            }

            CollectionUiEvent.ExitAddModeClicked -> {
                updateState { it.copy(isAddMode = false, editingEntryId = null, selectedAddCard = null) }
            }

            is CollectionUiEvent.EditEntryClicked -> {
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

            is CollectionUiEvent.RemoveEntryClicked -> {
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

            is CollectionUiEvent.AddToSellClicked -> {
                updateState { state ->
                    val entry = state.collectionEntries.firstOrNull { it.entryId == event.entryId }
                        ?: return@updateState state.copy(infoMessage = "Entry not found")

                    sellListTransferStore.addEntry(
                        entry.copy(entryId = "sell-${entry.entryId}-${nextEntryNumber++}")
                    )

                    state.copy(infoMessage = "Added ${entry.card.name} to Sell List")
                }
            }

            is CollectionUiEvent.AddSearchChanged -> {
                updateState { it.copy(addSearchQuery = event.value) }
            }

            CollectionUiEvent.AddSearchSubmitted -> {
                loadAddResults()
            }

            is CollectionUiEvent.AddResultClicked -> {
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

            is CollectionUiEvent.QuantityChanged -> {
                updateState { it.copy(addQuantityInput = event.value.filter(Char::isDigit)) }
            }

            is CollectionUiEvent.FoilSelected -> {
                updateState { it.copy(addFoil = event.value) }
            }

            is CollectionUiEvent.LanguageSelected -> {
                updateState { it.copy(addLanguage = event.value) }
            }

            is CollectionUiEvent.ConditionSelected -> {
                updateState { it.copy(addCondition = event.value) }
            }

            is CollectionUiEvent.ArtSelected -> {
                updateState { it.copy(selectedAddArtId = event.artId) }
            }

            CollectionUiEvent.DismissAddCardParametersClicked -> {
                updateState {
                    it.copy(
                        editingEntryId = null,
                        selectedAddCard = null,
                        addArtOptions = emptyList(),
                        selectedAddArtId = "",
                    )
                }
            }

            CollectionUiEvent.ConfirmAddClicked -> {
                val before = state.value.data.collectionEntries
                updateState { state ->
                    val card = state.selectedAddCard
                        ?: return@updateState state.copy(infoMessage = "Select card in add results")
                    val quantity = state.addQuantityInput.toIntOrNull()
                        ?: return@updateState state.copy(infoMessage = "Invalid quantity")
                    if (quantity <= 0) {
                        return@updateState state.copy(infoMessage = "Quantity must be > 0")
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
                        infoMessage = if (state.editingEntryId == null) {
                            "Added ${card.name} to collection"
                        } else {
                            "Updated ${card.name}"
                        },
                    )
                }
                syncEntriesIfChanged(before)
            }

            is CollectionUiEvent.ImportCsvReceived -> importFromCsv(event.content)

            CollectionUiEvent.DeleteAllClicked -> {
                val before = state.value.data.collectionEntries
                updateState { state ->
                    state.copy(
                        collectionEntries = emptyList(),
                        visibleCollectionEntries = emptyList(),
                        selectedCollectionEntryId = null,
                        collectionSearchQuery = "",
                        exportPreview = "",
                        infoMessage = "Collection cleared",
                        syncProgress = if (state.collectionEntries.isEmpty()) null else 0f,
                    )
                }
                val uid = currentUid
                val idToken = currentIdToken
                if (before.isNotEmpty() && uid != null && idToken != null) {
                    val scheduled = scheduleCollectionBackgroundDelete(uid = uid, idToken = idToken)
                    if (scheduled) {
                        updateState {
                            it.copy(
                                syncProgress = null,
                                infoMessage = "Collection delete queued in background",
                            )
                        }
                        return
                    }
                }
                if (before.isNotEmpty()) {
                    launch {
                        setLoading(true)
                        setError(null)
                        try {
                            updateState { it.copy(syncProgress = 0.2f) }
                            val persisted = persistEntriesNow(emptyList())
                            if (persisted) {
                                updateState { it.copy(syncProgress = 0.7f) }
                                refreshCollectionFromRemote(successMessage = "Collection cleared")
                                updateState { it.copy(syncProgress = 1f) }
                                delay(PROGRESS_COMPLETE_HOLD_MS)
                            }
                        } finally {
                            setLoading(false)
                            updateState { it.copy(syncProgress = null) }
                        }
                    }
                }
            }

            CollectionUiEvent.ExportClicked -> {
                updateState { state ->
                    val text = if (state.collectionEntries.isEmpty()) {
                        "Collection is empty"
                    } else {
                        state.collectionEntries.joinToString("\n") { entry ->
                            "${entry.quantity}x ${entry.card.name} | ${entry.foil.label} | ${entry.language.label} | ${entry.condition.label} | ${entry.artLabel}"
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

    private fun importFromCsv(content: String?) {
        if (content.isNullOrBlank()) {
            updateState { it.copy(infoMessage = "No CSV selected", importProgress = null) }
            return
        }

        launch {
            setLoading(true)
            setError(null)
            val before = state.value.data.collectionEntries

            try {
                val rows = parseCollectionCsv(content)
                if (rows.isEmpty()) {
                    updateState { it.copy(infoMessage = "CSV has no valid rows", importProgress = null) }
                    return@launch
                }

                updateState { it.copy(importProgress = 0f, infoMessage = "Preparing import...") }

                val uniqueNames = rows
                    .map { it.name.trim() }
                    .filter { it.isNotBlank() }
                    .map { it.lowercase() to it }
                    .groupBy({ it.first }, { it.second })
                    .mapValues { it.value.first() }
                updateState {
                    it.copy(
                        importProgress = IMPORT_PROGRESS_PREPARE_END,
                        infoMessage = "Resolving ${uniqueNames.size} unique card names...",
                    )
                }

                val uniqueNameValues = uniqueNames.values.toList()
                val resolveBatches = uniqueNameValues.chunked(BULK_RESOLVE_PROGRESS_BATCH_SIZE)
                val resolveBatchCount = resolveBatches.size.coerceAtLeast(1)
                val resolvedFromLocalIndex = mutableMapOf<String, MtgCard>()
                resolveBatches.forEachIndexed { index, batch ->
                    val resolvedBatch = tradeService.resolveCardsByExactNames(batch.toSet()).mapKeys { it.key.lowercase() }
                    resolvedFromLocalIndex.putAll(resolvedBatch)

                    val batchPart = (index + 1).toFloat() / resolveBatchCount.toFloat()
                    val currentProgress = IMPORT_PROGRESS_PREPARE_END +
                        (batchPart * (IMPORT_PROGRESS_BULK_RESOLVE_END - IMPORT_PROGRESS_PREPARE_END))
                    updateState {
                        it.copy(
                            importProgress = currentProgress.coerceIn(0f, 1f),
                            infoMessage = "Resolving cards ${index + 1}/$resolveBatchCount...",
                        )
                    }
                    yield()
                }

                val unresolved = uniqueNames.filterKeys { key -> !resolvedFromLocalIndex.containsKey(key) }.toList()
                var shouldThrottleBeforeApiCall = false
                val unresolvedTotal = unresolved.size.coerceAtLeast(1)
                unresolved.forEachIndexed { index, (normalized, originalName) ->
                    if (resolvedFromLocalIndex.containsKey(normalized)) return@forEachIndexed
                    val fallback = resolveCardByNameWithRetry(
                        cardName = originalName,
                        throttleBeforeRequest = shouldThrottleBeforeApiCall,
                    )
                    shouldThrottleBeforeApiCall = true
                    if (fallback != null) {
                        resolvedFromLocalIndex[normalized] = fallback
                    }

                    val fallbackPart = (index + 1).toFloat() / unresolvedTotal.toFloat()
                    val currentProgress = IMPORT_PROGRESS_BULK_RESOLVE_END +
                        (fallbackPart * (IMPORT_PROGRESS_FALLBACK_END - IMPORT_PROGRESS_BULK_RESOLVE_END))
                    updateState {
                        it.copy(
                            importProgress = currentProgress.coerceIn(0f, 1f),
                            infoMessage = "Resolving fallback cards ${index + 1}/$unresolvedTotal...",
                        )
                    }
                    yield()
                }
                if (unresolved.isEmpty()) {
                    updateState { it.copy(importProgress = IMPORT_PROGRESS_FALLBACK_END) }
                }

                val imported = buildList {
                    val total = rows.size.coerceAtLeast(1)
                    rows.forEachIndexed { index, row ->
                        val buildPart = (index + 1).toFloat() / total.toFloat()
                        val currentProgress = IMPORT_PROGRESS_FALLBACK_END +
                            (buildPart * (IMPORT_PROGRESS_BUILD_END - IMPORT_PROGRESS_FALLBACK_END))
                        updateState {
                            it.copy(
                                importProgress = currentProgress.coerceIn(0f, 1f),
                                infoMessage = "Preparing entries ${index + 1}/$total...",
                            )
                        }
                        yield()

                        val name = row.name.trim()
                        if (name.isBlank()) {
                            return@forEachIndexed
                        }
                        val quantity = row.count.coerceAtLeast(1)
                        val cacheKey = name.lowercase()

                        val resolvedCard = resolvedFromLocalIndex[cacheKey] ?: MtgCard(
                            id = "import-${name.lowercase().replace("[^a-z0-9]+".toRegex(), "-")}-${nextEntryNumber}",
                            name = name,
                            typeLine = row.edition.ifBlank { "Unknown" },
                            imageUrl = null,
                        )

                        add(
                            CollectionCardEntry(
                                entryId = "entry-${nextEntryNumber++}",
                                card = resolvedCard,
                                quantity = quantity,
                                foil = FoilOption.NON_FOIL,
                                language = row.language.toLanguageOption(),
                                condition = row.condition.toConditionOption(),
                                artLabel = "Default art",
                                artImageUrl = resolvedCard.imageUrl,
                            )
                        )
                    }
                }

                if (imported.isEmpty()) {
                    updateState { it.copy(infoMessage = "CSV has no valid rows", importProgress = null, syncProgress = null) }
                    return@launch
                }

                val current = state.value.data
                val merged = mergeDuplicateEntries(current.collectionEntries + imported)
                val filtered = filterCollection(merged, current.collectionSearchQuery)

                updateState { currentState ->
                    currentState.copy(
                        collectionEntries = merged,
                        visibleCollectionEntries = filtered,
                        selectedCollectionEntryId = filtered.firstOrNull()?.entryId,
                        infoMessage = "Saving imported cards...",
                    )
                }

                if (merged != before) {
                    updateState { it.copy(importProgress = IMPORT_PROGRESS_SAVE_START) }
                    val persisted = persistEntriesNow(merged)
                    if (persisted) {
                        updateState { it.copy(importProgress = IMPORT_PROGRESS_SAVE_END) }
                        refreshCollectionFromRemote(
                            successMessage = "Imported ${imported.size} cards from CSV",
                        )
                    } else {
                        updateState { it.copy(infoMessage = "Imported ${imported.size} cards from CSV") }
                    }
                } else {
                    updateState { it.copy(infoMessage = "Imported ${imported.size} cards from CSV") }
                }
                updateState { it.copy(importProgress = 1f) }
                delay(PROGRESS_COMPLETE_HOLD_MS)
            } catch (e: Throwable) {
                setError(e.message ?: "Failed to import CSV")
                updateState { it.copy(infoMessage = "CSV import failed", importProgress = null, syncProgress = null) }
            } finally {
                setLoading(false)
                updateState { it.copy(importProgress = null, syncProgress = null) }
            }
        }
    }

    private suspend fun resolveCardByNameWithRetry(
        cardName: String,
        throttleBeforeRequest: Boolean,
    ): MtgCard? {
        var shouldThrottle = throttleBeforeRequest
        var backoffMs = INITIAL_429_BACKOFF_MS

        repeat(MAX_SCRYFALL_RETRIES) { attempt ->
            if (shouldThrottle) {
                delay(SCRYFALL_THROTTLE_MS)
            }
            shouldThrottle = true

            runCatching {
                tradeService.searchCards(query = cardName, filter = TradeFilter.ALL)
            }.onSuccess { cards ->
                return cards.firstOrNull { it.name.equals(cardName, ignoreCase = true) } ?: cards.firstOrNull()
            }.onFailure { throwable ->
                val isLastAttempt = attempt == MAX_SCRYFALL_RETRIES - 1
                if (isScryfallRateLimitError(throwable) && !isLastAttempt) {
                    delay(backoffMs)
                    backoffMs *= 2
                } else if (!isLastAttempt) {
                    // Non-429 failures should not fail whole import; return fallback card for this row.
                    return null
                }
            }
        }

        return null
    }

    private fun isScryfallRateLimitError(throwable: Throwable): Boolean {
        val message = throwable.message.orEmpty()
        return "429" in message || "Too Many Requests" in message
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

        domainCall(
            loading = null,
            clearErrorOnStart = false,
            onError = { throwable ->
                setError(throwable.message ?: "Failed to sync collection")
            },
            action = {
                tradeService.replaceListEntries(
                    context = AuthContext(uid = uid, idToken = idToken),
                    listType = TradeListType.COLLECTION,
                    entries = entries,
                )
            },
        )
    }

    private suspend fun persistEntriesNow(entries: List<CollectionCardEntry>): Boolean {
        val uid = currentUid ?: return false
        val idToken = currentIdToken ?: return false
        val payload = entries.map { it.toStoredTradeCardEntry() }

        return runCatching {
            tradeService.replaceListEntries(
                context = AuthContext(uid = uid, idToken = idToken),
                listType = TradeListType.COLLECTION,
                entries = payload,
            )
        }.onFailure {
            setError(it.message ?: "Failed to sync collection")
        }.isSuccess
    }

    private suspend fun refreshCollectionFromRemote(successMessage: String? = null) {
        val uid = currentUid ?: return
        val idToken = currentIdToken ?: return

        runCatching {
            tradeService.loadListEntries(
                context = AuthContext(uid = uid, idToken = idToken),
                listType = TradeListType.COLLECTION,
            )
        }.onSuccess { remoteEntries ->
            val mapped = remoteEntries.map { it.toCollectionCardEntry() }
            nextEntryNumber = nextEntryNumber(mapped)

            updateState { current ->
                val filtered = filterCollection(mapped, current.collectionSearchQuery)
                current.copy(
                    collectionEntries = mapped,
                    visibleCollectionEntries = filtered,
                    selectedCollectionEntryId = filtered.firstOrNull()?.entryId,
                    infoMessage = successMessage ?: current.infoMessage,
                )
            }
        }.onFailure {
            setError(it.message ?: "Failed to refresh collection")
        }
    }

    private fun loadPersistedEntries(uid: String, idToken: String) {
        domainCall(
            action = {
                tradeService.loadListEntries(
                    context = AuthContext(uid = uid, idToken = idToken),
                    listType = TradeListType.COLLECTION,
                ).map { it.toCollectionCardEntry() }
            },
            onError = { throwable ->
                setError(throwable.message ?: "Failed to load collection")
            },
        ) { entries ->
            nextEntryNumber = nextEntryNumber(entries)
            updateState {
                it.copy(
                    collectionEntries = entries,
                    visibleCollectionEntries = entries,
                    selectedCollectionEntryId = entries.firstOrNull()?.entryId,
                    infoMessage = if (entries.isEmpty()) {
                        "Collection is empty"
                    } else {
                        "Loaded ${entries.size} items"
                    },
                )
            }
        }
    }
}

private fun filterCollection(
    entries: List<CollectionCardEntry>,
    query: String,
): List<CollectionCardEntry> {
    if (query.isBlank()) return entries
    val needle = query.trim()
    return entries.filter { it.card.name.contains(needle, ignoreCase = true) }
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

private fun nextEntryNumber(entries: List<CollectionCardEntry>): Int {
    val maxId = entries.mapNotNull { entry ->
        entry.entryId.removePrefix("entry-").toIntOrNull()
    }.maxOrNull() ?: 0
    return maxId + 1
}

private data class CsvCollectionRow(
    val count: Int,
    val name: String,
    val edition: String,
    val condition: String,
    val language: String,
)

private fun parseCollectionCsv(csv: String): List<CsvCollectionRow> {
    val lines = csv
        .lineSequence()
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .toList()
    if (lines.size < 2) return emptyList()

    val delimiter = detectCsvDelimiter(lines.first())
    val header = parseCsvLine(lines.first(), delimiter).map { it.trim().lowercase() }
    val countIndex = header.indexOf("count")
    val tradelistCountIndex = header.indexOf("tradelist count")
    val nameIndex = header.indexOf("name")
    val editionIndex = header.indexOf("edition")
    val conditionIndex = header.indexOf("condition")
    val languageIndex = header.indexOf("language")
    if (nameIndex < 0) return emptyList()

    return lines.drop(1).mapNotNull { line ->
        val cols = parseCsvLine(line, delimiter)
        val name = cols.getOrNull(nameIndex).orEmpty().trim()
        if (name.isBlank()) return@mapNotNull null
        val count = when {
            countIndex >= 0 -> cols.getOrNull(countIndex).orEmpty().trim().toIntOrNull()
            else -> null
        } ?: when {
            tradelistCountIndex >= 0 -> cols.getOrNull(tradelistCountIndex).orEmpty().trim().toIntOrNull()
            else -> null
        } ?: 1
        CsvCollectionRow(
            count = count,
            name = name,
            edition = cols.getOrNull(editionIndex).orEmpty().trim(),
            condition = cols.getOrNull(conditionIndex).orEmpty().trim(),
            language = cols.getOrNull(languageIndex).orEmpty().trim(),
        )
    }
}

private fun detectCsvDelimiter(headerLine: String): Char {
    val commas = headerLine.count { it == ',' }
    val semicolons = headerLine.count { it == ';' }
    return if (semicolons > commas) ';' else ','
}

private fun parseCsvLine(line: String, delimiter: Char): List<String> {
    val result = mutableListOf<String>()
    val current = StringBuilder()
    var inQuotes = false
    var i = 0

    while (i < line.length) {
        val ch = line[i]
        when {
            ch == '"' -> {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                    current.append('"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            }

            ch == delimiter && !inQuotes -> {
                result += current.toString()
                current.clear()
            }

            else -> current.append(ch)
        }
        i++
    }

    result += current.toString()
    return result
}

private fun String.toConditionOption(): CardCondition {
    return when (trim().lowercase()) {
        "nm", "near mint", "mint" -> CardCondition.NM
        "exc", "excellent", "ex" -> CardCondition.EXC
        "pl", "played", "lp", "light played", "mp", "moderately played" -> CardCondition.PL
        "poor", "hp", "heavily played", "damaged" -> CardCondition.POOR
        else -> CardCondition.NM
    }
}

private fun String.toLanguageOption(): LanguageOption {
    return when (trim().lowercase()) {
        "en", "english" -> LanguageOption.EN
        "de", "german" -> LanguageOption.DE
        "cz", "czech" -> LanguageOption.CZ
        "fr", "french" -> LanguageOption.FR
        else -> LanguageOption.EN
    }
}

private const val SCRYFALL_THROTTLE_MS = 150L
private const val INITIAL_429_BACKOFF_MS = 1_000L
private const val MAX_SCRYFALL_RETRIES = 4
private const val PROGRESS_COMPLETE_HOLD_MS = 350L
private const val IMPORT_PROGRESS_PREPARE_END = 0.08f
private const val IMPORT_PROGRESS_BULK_RESOLVE_END = 0.35f
private const val IMPORT_PROGRESS_FALLBACK_END = 0.70f
private const val IMPORT_PROGRESS_BUILD_END = 0.92f
private const val IMPORT_PROGRESS_SAVE_START = 0.95f
private const val IMPORT_PROGRESS_SAVE_END = 0.99f
private const val BULK_RESOLVE_PROGRESS_BATCH_SIZE = 60
