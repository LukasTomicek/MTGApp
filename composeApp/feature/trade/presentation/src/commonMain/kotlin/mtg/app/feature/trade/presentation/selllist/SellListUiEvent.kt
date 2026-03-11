package mtg.app.feature.trade.presentation.selllist

import mtg.app.core.presentation.Event
import mtg.app.feature.trade.presentation.utils.model.CardCondition
import mtg.app.feature.trade.presentation.utils.model.FoilOption
import mtg.app.feature.trade.presentation.utils.model.LanguageOption
import mtg.app.feature.map.presentation.map.MapCoordinate

sealed interface SellListUiEvent : Event {
    data class CollectionSearchChanged(val value: String) : SellListUiEvent
    data class CollectionCardClicked(val entryId: String) : SellListUiEvent
    data class EditEntryClicked(val entryId: String) : SellListUiEvent
    data class RemoveEntryClicked(val entryId: String) : SellListUiEvent
    data object EnterAddModeClicked : SellListUiEvent
    data object ExitAddModeClicked : SellListUiEvent
    data class AddSearchChanged(val value: String) : SellListUiEvent
    data object AddSearchSubmitted : SellListUiEvent
    data class AddResultClicked(val cardId: String) : SellListUiEvent
    data class QuantityChanged(val value: String) : SellListUiEvent
    data class FoilSelected(val value: FoilOption) : SellListUiEvent
    data class LanguageSelected(val value: LanguageOption) : SellListUiEvent
    data class ConditionSelected(val value: CardCondition) : SellListUiEvent
    data class PriceChanged(val value: String) : SellListUiEvent
    data class ArtSelected(val artId: String) : SellListUiEvent
    data object DismissAddCardParametersClicked : SellListUiEvent
    data object ConfirmAddClicked : SellListUiEvent
    data object DismissPinRecommendationClicked : SellListUiEvent
    data class CurrentLocationResolved(val coordinate: MapCoordinate?) : SellListUiEvent
    data object ImportSampleClicked : SellListUiEvent
    data object ExportClicked : SellListUiEvent
}
