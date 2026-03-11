package mtg.app.feature.trade.presentation.collection

import mtg.app.core.presentation.Event
import mtg.app.feature.trade.presentation.utils.model.CardCondition
import mtg.app.feature.trade.presentation.utils.model.FoilOption
import mtg.app.feature.trade.presentation.utils.model.LanguageOption

sealed interface CollectionUiEvent : Event {
    data class CollectionSearchChanged(val value: String) : CollectionUiEvent
    data class CollectionCardClicked(val entryId: String) : CollectionUiEvent
    data class EditEntryClicked(val entryId: String) : CollectionUiEvent
    data class RemoveEntryClicked(val entryId: String) : CollectionUiEvent
    data class AddToSellClicked(val entryId: String) : CollectionUiEvent
    data object EnterAddModeClicked : CollectionUiEvent
    data object ExitAddModeClicked : CollectionUiEvent
    data class AddSearchChanged(val value: String) : CollectionUiEvent
    data object AddSearchSubmitted : CollectionUiEvent
    data class AddResultClicked(val cardId: String) : CollectionUiEvent
    data class QuantityChanged(val value: String) : CollectionUiEvent
    data class FoilSelected(val value: FoilOption) : CollectionUiEvent
    data class LanguageSelected(val value: LanguageOption) : CollectionUiEvent
    data class ConditionSelected(val value: CardCondition) : CollectionUiEvent
    data class ArtSelected(val artId: String) : CollectionUiEvent
    data object DismissAddCardParametersClicked : CollectionUiEvent
    data object ConfirmAddClicked : CollectionUiEvent
    data class ImportCsvReceived(val content: String?) : CollectionUiEvent
    data object DeleteAllClicked : CollectionUiEvent
    data object ExportClicked : CollectionUiEvent
}
