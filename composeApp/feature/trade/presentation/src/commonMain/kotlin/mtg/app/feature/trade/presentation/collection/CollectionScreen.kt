package mtg.app.feature.trade.presentation.collection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import mtg.app.core.presentation.components.AppAlertDialog
import mtg.app.core.presentation.state.UiState
import mtg.app.feature.trade.presentation.utils.component.AddCardParametersPanel
import mtg.app.feature.trade.presentation.utils.component.ImportModal
import mtg.app.feature.trade.presentation.utils.component.TradeListAddModeContent
import mtg.app.feature.trade.presentation.utils.component.TradeListCollectionModeContent
import mtg.app.feature.trade.presentation.utils.rememberCollectionCsvPicker

@Composable
fun CollectionScreen(
    uiState: UiState<CollectionScreenState>,
    onUiEvent: (CollectionUiEvent) -> Unit,
) {
    var showImportDialog by remember { mutableStateOf(false) }
    var csvFileLoadProgress by remember { mutableStateOf<Float?>(null) }
    var pendingCsvContent by remember { mutableStateOf<String?>(null) }
    var pendingCsvFileName by remember { mutableStateOf("") }
    val openCsvPicker = rememberCollectionCsvPicker(
        onFileLoaded = { fileName, content ->
            csvFileLoadProgress = null
            pendingCsvContent = content
            pendingCsvFileName = fileName
            showImportDialog = true
        },
        onProgress = { progress ->
            csvFileLoadProgress = progress
        },
        onFailure = {
            csvFileLoadProgress = null
            onUiEvent(CollectionUiEvent.ImportCsvReceived(null))
        },
    )

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
            addArtOptions = uiState.data.addArtOptions,
            selectedAddArtId = uiState.data.selectedAddArtId,
            isEditing = uiState.data.editingEntryId != null,
            infoMessage = uiState.data.infoMessage,
            onAddSearchChanged = { onUiEvent(CollectionUiEvent.AddSearchChanged(it)) },
            onAddSearchSubmitted = { onUiEvent(CollectionUiEvent.AddSearchSubmitted) },
            onAddResultClicked = { onUiEvent(CollectionUiEvent.AddResultClicked(it)) },
            onQuantityChanged = { onUiEvent(CollectionUiEvent.QuantityChanged(it)) },
            onFoilSelected = { onUiEvent(CollectionUiEvent.FoilSelected(it)) },
            onLanguageSelected = { onUiEvent(CollectionUiEvent.LanguageSelected(it)) },
            onConditionSelected = { onUiEvent(CollectionUiEvent.ConditionSelected(it)) },
            onArtSelected = { onUiEvent(CollectionUiEvent.ArtSelected(it)) },
            onDismissCardParameters = { onUiEvent(CollectionUiEvent.DismissAddCardParametersClicked) },
            onConfirm = { onUiEvent(CollectionUiEvent.ConfirmAddClicked) },
        )
    } else {
        TradeListCollectionModeContent(
            collectionSearchQuery = uiState.data.collectionSearchQuery,
            visibleEntries = uiState.data.visibleCollectionEntries,
            allEntries = uiState.data.collectionEntries,
            selectedEntryId = uiState.data.selectedCollectionEntryId,
            infoMessage = uiState.data.infoMessage,
            exportPreview = uiState.data.exportPreview,
            onSearchChanged = { onUiEvent(CollectionUiEvent.CollectionSearchChanged(it)) },
            onEnterAddMode = { onUiEvent(CollectionUiEvent.EnterAddModeClicked) },
            onCardClicked = { onUiEvent(CollectionUiEvent.CollectionCardClicked(it)) },
            onEditClicked = { onUiEvent(CollectionUiEvent.EditEntryClicked(it)) },
            onDeleteClicked = { onUiEvent(CollectionUiEvent.RemoveEntryClicked(it)) },
            onMoveToSellClicked = { onUiEvent(CollectionUiEvent.AddToSellClicked(it)) },
            onImportClick = {
                csvFileLoadProgress = 0f
                openCsvPicker()
            },
            onExportClick = { onUiEvent(CollectionUiEvent.ExportClicked) },
            onDeleteAllClicked = { onUiEvent(CollectionUiEvent.DeleteAllClicked) },
            isBusy = uiState.isLoading || csvFileLoadProgress != null,
            showPriceInSubtitle = false,
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
                artOptions = uiState.data.addArtOptions,
                selectedArtId = uiState.data.selectedAddArtId,
                isEditing = true,
                onQuantityChanged = { onUiEvent(CollectionUiEvent.QuantityChanged(it)) },
                onFoilSelected = { onUiEvent(CollectionUiEvent.FoilSelected(it)) },
                onLanguageSelected = { onUiEvent(CollectionUiEvent.LanguageSelected(it)) },
                onConditionSelected = { onUiEvent(CollectionUiEvent.ConditionSelected(it)) },
                onArtSelected = { onUiEvent(CollectionUiEvent.ArtSelected(it)) },
                onDismiss = { onUiEvent(CollectionUiEvent.DismissAddCardParametersClicked) },
                onConfirm = { onUiEvent(CollectionUiEvent.ConfirmAddClicked) },
            )
        }
    }

    val isBusy = uiState.isLoading || csvFileLoadProgress != null
    if (isBusy) {
        ImportModal(
            csvFileLoadProgress = csvFileLoadProgress,
            importProgress = uiState.data.importProgress,
            syncProgress = uiState.data.syncProgress,
        )
    }

    if (showImportDialog) {
        AppAlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = "Import CSV",
            message = "Soubor $pendingCsvFileName je pripraven k importu.",
            confirmText = "Vlozit CSV",
            dismissText = "Zavrit",
            onConfirmClick = {
                onUiEvent(CollectionUiEvent.ImportCsvReceived(pendingCsvContent))
                showImportDialog = false
                pendingCsvContent = null
                pendingCsvFileName = ""
            },
            onDismissClick = {
                showImportDialog = false
                pendingCsvContent = null
                pendingCsvFileName = ""
            },
        )
    }
}
