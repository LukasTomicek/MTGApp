package mtg.app.feature.trade.presentation.buylist

import androidx.compose.runtime.Composable
import mtg.app.core.presentation.state.UiState
import mtg.app.feature.map.presentation.utils.rememberCurrentLocationRequester
import mtg.app.feature.trade.presentation.utils.component.AddCardParametersPanel
import mtg.app.feature.trade.presentation.utils.component.PinRecommendationDialog
import mtg.app.feature.trade.presentation.utils.component.TradeListAddModeContent
import mtg.app.feature.trade.presentation.utils.component.TradeListCollectionModeContent

@Composable
fun BuyListScreen(
    uiState: UiState<BuyListScreenState>,
    onUiEvent: (BuyListUiEvent) -> Unit,
) {
    val requestCurrentLocation = rememberCurrentLocationRequester { location ->
        onUiEvent(BuyListUiEvent.CurrentLocationResolved(location))
    }

    if (uiState.data.isAddMode) {
        TradeListAddModeContent(
            isLoading = uiState.isLoading,
            error = uiState.error,
            addSearchQuery = uiState.data.addSearchQuery,
            addResults = uiState.data.addResults,
            selectedAddCard = uiState.data.selectedAddCard,
            addQuantityInput = uiState.data.addQuantityInput,
            addFoil = uiState.data.addFoil,
            addLanguage = uiState.data.addLanguage,
            addCondition = uiState.data.addCondition,
            addPriceInput = uiState.data.addPriceInput,
            addArtOptions = uiState.data.addArtOptions,
            selectedAddArtId = uiState.data.selectedAddArtId,
            isEditing = uiState.data.editingEntryId != null,
            infoMessage = uiState.data.infoMessage,
            onAddSearchChanged = { onUiEvent(BuyListUiEvent.AddSearchChanged(it)) },
            onAddSearchSubmitted = { onUiEvent(BuyListUiEvent.AddSearchSubmitted) },
            onAddResultClicked = { onUiEvent(BuyListUiEvent.AddResultClicked(it)) },
            onQuantityChanged = { onUiEvent(BuyListUiEvent.QuantityChanged(it)) },
            onFoilSelected = { onUiEvent(BuyListUiEvent.FoilSelected(it)) },
            onLanguageSelected = { onUiEvent(BuyListUiEvent.LanguageSelected(it)) },
            onConditionSelected = { onUiEvent(BuyListUiEvent.ConditionSelected(it)) },
            onPriceChanged = { onUiEvent(BuyListUiEvent.PriceChanged(it)) },
            onArtSelected = { onUiEvent(BuyListUiEvent.ArtSelected(it)) },
            onDismissCardParameters = { onUiEvent(BuyListUiEvent.DismissAddCardParametersClicked) },
            onConfirm = { onUiEvent(BuyListUiEvent.ConfirmAddClicked) },
        )
    } else {
        TradeListCollectionModeContent(
            collectionSearchQuery = uiState.data.collectionSearchQuery,
            visibleEntries = uiState.data.visibleCollectionEntries,
            selectedEntryId = uiState.data.selectedCollectionEntryId,
            infoMessage = uiState.data.infoMessage,
            exportPreview = uiState.data.exportPreview,
            onSearchChanged = { onUiEvent(BuyListUiEvent.CollectionSearchChanged(it)) },
            onEnterAddMode = { onUiEvent(BuyListUiEvent.EnterAddModeClicked) },
            onCardClicked = { onUiEvent(BuyListUiEvent.CollectionCardClicked(it)) },
            onEditClicked = { onUiEvent(BuyListUiEvent.EditEntryClicked(it)) },
            onDeleteClicked = { onUiEvent(BuyListUiEvent.RemoveEntryClicked(it)) },
        )
    }

    if (uiState.data.showPinRecommendationDialog) {
        PinRecommendationDialog(
            onDismiss = { onUiEvent(BuyListUiEvent.DismissPinRecommendationClicked) },
            onAddPinNow = requestCurrentLocation,
        )
    }

    if (!uiState.data.isAddMode && uiState.data.editingEntryId != null) {
        uiState.data.selectedAddCard?.let { card ->
            AddCardParametersPanel(
                card = card,
                quantity = uiState.data.addQuantityInput,
                foil = uiState.data.addFoil,
                language = uiState.data.addLanguage,
                condition = uiState.data.addCondition,
                price = uiState.data.addPriceInput,
                artOptions = uiState.data.addArtOptions,
                selectedArtId = uiState.data.selectedAddArtId,
                isEditing = true,
                onQuantityChanged = { onUiEvent(BuyListUiEvent.QuantityChanged(it)) },
                onFoilSelected = { onUiEvent(BuyListUiEvent.FoilSelected(it)) },
                onLanguageSelected = { onUiEvent(BuyListUiEvent.LanguageSelected(it)) },
                onConditionSelected = { onUiEvent(BuyListUiEvent.ConditionSelected(it)) },
                onPriceChanged = { onUiEvent(BuyListUiEvent.PriceChanged(it)) },
                onArtSelected = { onUiEvent(BuyListUiEvent.ArtSelected(it)) },
                onDismiss = { onUiEvent(BuyListUiEvent.DismissAddCardParametersClicked) },
                onConfirm = { onUiEvent(BuyListUiEvent.ConfirmAddClicked) },
            )
        }
    }
}
