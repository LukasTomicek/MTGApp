package mtg.app.feature.trade.presentation.collection

import mtg.app.feature.trade.domain.MtgCard
import mtg.app.feature.trade.presentation.utils.model.CardCondition
import mtg.app.feature.trade.presentation.utils.model.CollectionArtOption
import mtg.app.feature.trade.presentation.utils.model.CollectionCardEntry
import mtg.app.feature.trade.presentation.utils.model.FoilOption
import mtg.app.feature.trade.presentation.utils.model.LanguageOption

data class CollectionScreenState(
    // Collection list
    val collectionSearchQuery: String = "",
    val collectionEntries: List<CollectionCardEntry> = emptyList(),
    val visibleCollectionEntries: List<CollectionCardEntry> = emptyList(),
    val selectedCollectionEntryId: String? = null,

    // Add/Edit mode
    val isAddMode: Boolean = false,
    val editingEntryId: String? = null,

    // Card search
    val addSearchQuery: String = "",
    val addResults: List<MtgCard> = emptyList(),
    val selectedAddCard: MtgCard? = null,

    // Card parameters
    val addQuantityInput: String = "1",
    val addFoil: FoilOption = FoilOption.NON_FOIL,
    val addLanguage: LanguageOption = LanguageOption.EN,
    val addCondition: CardCondition = CardCondition.NM,
    val addArtOptions: List<CollectionArtOption> = emptyList(),
    val selectedAddArtId: String = "",

    // Import/Sync progress
    val importProgress: Float? = null,
    val syncProgress: Float? = null,

    // UI info/output
    val exportPreview: String = "",
    val infoMessage: String = "",
)
