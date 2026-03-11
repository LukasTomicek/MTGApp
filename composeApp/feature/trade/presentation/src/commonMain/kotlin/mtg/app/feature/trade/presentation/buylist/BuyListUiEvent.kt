package mtg.app.feature.trade.presentation.buylist

import mtg.app.core.presentation.Event
import mtg.app.feature.trade.presentation.utils.model.CardCondition
import mtg.app.feature.trade.presentation.utils.model.FoilOption
import mtg.app.feature.trade.presentation.utils.model.LanguageOption
import mtg.app.feature.map.presentation.map.MapCoordinate

sealed interface BuyListUiEvent : Event {
    data class CollectionSearchChanged(val value: String) : BuyListUiEvent
    data class CollectionCardClicked(val entryId: String) : BuyListUiEvent
    data class EditEntryClicked(val entryId: String) : BuyListUiEvent
    data class RemoveEntryClicked(val entryId: String) : BuyListUiEvent
    data object EnterAddModeClicked : BuyListUiEvent
    data object ExitAddModeClicked : BuyListUiEvent
    data class AddSearchChanged(val value: String) : BuyListUiEvent
    data object AddSearchSubmitted : BuyListUiEvent
    data class AddResultClicked(val cardId: String) : BuyListUiEvent
    data class QuantityChanged(val value: String) : BuyListUiEvent
    data class FoilSelected(val value: FoilOption) : BuyListUiEvent
    data class LanguageSelected(val value: LanguageOption) : BuyListUiEvent
    data class ConditionSelected(val value: CardCondition) : BuyListUiEvent
    data class PriceChanged(val value: String) : BuyListUiEvent
    data class ArtSelected(val artId: String) : BuyListUiEvent
    data object DismissAddCardParametersClicked : BuyListUiEvent
    data object ConfirmAddClicked : BuyListUiEvent
    data object DismissPinRecommendationClicked : BuyListUiEvent
    data class CurrentLocationResolved(val coordinate: MapCoordinate?) : BuyListUiEvent
    data object ImportSampleClicked : BuyListUiEvent
    data object ExportClicked : BuyListUiEvent
}
