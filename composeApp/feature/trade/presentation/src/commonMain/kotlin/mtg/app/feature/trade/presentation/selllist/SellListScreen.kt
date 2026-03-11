package mtg.app.feature.trade.presentation.selllist

import androidx.compose.runtime.Composable
import mtg.app.core.presentation.state.UiState
import mtg.app.feature.map.presentation.utils.rememberCurrentLocationRequester
import mtg.app.feature.trade.presentation.utils.component.AddCardParametersPanel
import mtg.app.feature.trade.presentation.utils.component.PinRecommendationDialog
import mtg.app.feature.trade.presentation.utils.component.TradeListAddModeContent
import mtg.app.feature.trade.presentation.utils.component.TradeListCollectionModeContent

@Composable
fun SellListScreen(
    uiState: UiState<SellListScreenState>,
    onUiEvent: (SellListUiEvent) -> Unit,
) {
    val requestCurrentLocation = rememberCurrentLocationRequester { location ->
        onUiEvent(SellListUiEvent.CurrentLocationResolved(location))
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
            onAddSearchChanged = { onUiEvent(SellListUiEvent.AddSearchChanged(it)) },
            onAddSearchSubmitted = { onUiEvent(SellListUiEvent.AddSearchSubmitted) },
            onAddResultClicked = { onUiEvent(SellListUiEvent.AddResultClicked(it)) },
            onQuantityChanged = { onUiEvent(SellListUiEvent.QuantityChanged(it)) },
            onFoilSelected = { onUiEvent(SellListUiEvent.FoilSelected(it)) },
            onLanguageSelected = { onUiEvent(SellListUiEvent.LanguageSelected(it)) },
            onConditionSelected = { onUiEvent(SellListUiEvent.ConditionSelected(it)) },
            onPriceChanged = { onUiEvent(SellListUiEvent.PriceChanged(it)) },
            onArtSelected = { onUiEvent(SellListUiEvent.ArtSelected(it)) },
            onDismissCardParameters = { onUiEvent(SellListUiEvent.DismissAddCardParametersClicked) },
            onConfirm = { onUiEvent(SellListUiEvent.ConfirmAddClicked) },
        )
    } else {
        TradeListCollectionModeContent(
            collectionSearchQuery = uiState.data.collectionSearchQuery,
            visibleEntries = uiState.data.visibleCollectionEntries,
            selectedEntryId = uiState.data.selectedCollectionEntryId,
            infoMessage = uiState.data.infoMessage,
            exportPreview = uiState.data.exportPreview,
            onSearchChanged = { onUiEvent(SellListUiEvent.CollectionSearchChanged(it)) },
            onEnterAddMode = { onUiEvent(SellListUiEvent.EnterAddModeClicked) },
            onCardClicked = { onUiEvent(SellListUiEvent.CollectionCardClicked(it)) },
            onEditClicked = { onUiEvent(SellListUiEvent.EditEntryClicked(it)) },
            onDeleteClicked = { onUiEvent(SellListUiEvent.RemoveEntryClicked(it)) },
        )
    }

    if (uiState.data.showPinRecommendationDialog) {
        PinRecommendationDialog(
            onDismiss = { onUiEvent(SellListUiEvent.DismissPinRecommendationClicked) },
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
                onQuantityChanged = { onUiEvent(SellListUiEvent.QuantityChanged(it)) },
                onFoilSelected = { onUiEvent(SellListUiEvent.FoilSelected(it)) },
                onLanguageSelected = { onUiEvent(SellListUiEvent.LanguageSelected(it)) },
                onConditionSelected = { onUiEvent(SellListUiEvent.ConditionSelected(it)) },
                onPriceChanged = { onUiEvent(SellListUiEvent.PriceChanged(it)) },
                onArtSelected = { onUiEvent(SellListUiEvent.ArtSelected(it)) },
                onDismiss = { onUiEvent(SellListUiEvent.DismissAddCardParametersClicked) },
                onConfirm = { onUiEvent(SellListUiEvent.ConfirmAddClicked) },
            )
        }
    }
}
