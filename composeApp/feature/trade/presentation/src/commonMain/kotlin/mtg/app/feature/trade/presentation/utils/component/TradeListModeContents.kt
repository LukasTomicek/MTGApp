package mtg.app.feature.trade.presentation.utils.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mtg.app.core.presentation.components.AppButton
import mtg.app.core.presentation.components.AppButtonState
import mtg.app.core.presentation.components.CardCollectionRow
import mtg.app.core.presentation.components.CardImage
import mtg.app.core.presentation.components.TextInputRow
import mtg.app.core.presentation.components.TextState
import mtg.app.core.presentation.components.appOutlinedTextFieldColors
import mtg.app.feature.trade.domain.MtgCard
import mtg.app.feature.trade.presentation.utils.model.CardCondition
import mtg.app.feature.trade.presentation.utils.model.CollectionArtOption
import mtg.app.feature.trade.presentation.utils.model.CollectionCardEntry
import mtg.app.feature.trade.presentation.utils.model.FoilOption
import mtg.app.feature.trade.presentation.utils.model.LanguageOption

@Composable
fun TradeListCollectionModeContent(
    collectionSearchQuery: String,
    visibleEntries: List<CollectionCardEntry>,
    allEntries: List<CollectionCardEntry> = visibleEntries,
    selectedEntryId: String?,
    infoMessage: String,
    exportPreview: String,
    onSearchChanged: (String) -> Unit,
    onEnterAddMode: () -> Unit,
    onCardClicked: (String) -> Unit,
    onEditClicked: (String) -> Unit,
    onDeleteClicked: (String) -> Unit,
    onMoveToSellClicked: ((String) -> Unit)? = null,
    onImportClick: (() -> Unit)? = null,
    onExportClick: (() -> Unit)? = null,
    onDeleteAllClicked: (() -> Unit)? = null,
    searchLabel: String = "Find card in collection",
    isBusy: Boolean = false,
    showPriceInSubtitle: Boolean = true,
) {
    val totalCardsCount = allEntries.sumOf { it.quantity.coerceAtLeast(0) }
    val uniqueCardsCount = allEntries
        .map { it.card.name.trim().lowercase() }
        .filter { it.isNotBlank() }
        .distinct()
        .size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        OutlinedTextField(
            value = collectionSearchQuery,
            onValueChange = onSearchChanged,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isBusy,
            label = { Text(searchLabel) },
            colors = appOutlinedTextFieldColors(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppButton(
                modifier = Modifier.weight(1f),
                state = AppButtonState(
                    title = TextState("Add"),
                    enabled = !isBusy,
                    onClick = onEnterAddMode,
                ),
            )

            onImportClick?.let {
                AppButton(
                    modifier = Modifier.weight(1f),
                    state = AppButtonState(
                        title = TextState("Import"),
                        enabled = !isBusy,
                        onClick = it,
                    ),
                )
            }

            onExportClick?.let {
                AppButton(
                    modifier = Modifier.weight(1f),
                    state = AppButtonState(
                        title = TextState("Export"),
                        enabled = !isBusy,
                        onClick = it,
                    ),
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (visibleEntries.isEmpty()) {
                    item {
                        Text(
                            text = "Collection is empty",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(8.dp),
                        )
                    }
                }

                items(visibleEntries, key = { it.entryId }) { entry ->
                    val subtitle = "${entry.foil.label} | ${entry.language.label} | ${entry.condition.label} | ${entry.artLabel}"
                    val price = if (showPriceInSubtitle) entry.price?.let { "$$it" } ?: "-" else null

                    CardCollectionRow(
                        title = "${entry.quantity}x ${entry.card.name}",
                        subtitle = subtitle,
                        price = price,
                        selected = entry.entryId == selectedEntryId,
                        onRowClick = { onCardClicked(entry.entryId) },
                        onEditClick = { onEditClicked(entry.entryId) },
                        onDeleteClick = { onDeleteClicked(entry.entryId) },
                        onMoveToSellClick = onMoveToSellClicked?.let { callback -> { callback(entry.entryId) } },
                        leadingContent = {
                            CardImage(
                                imageUrl = entry.artImageUrl ?: entry.card.imageUrl,
                                heightDp = 46,
                            )
                        },
                    )
                }
            }
        }

        if (infoMessage.isNotBlank()) {
            Text(
                text = infoMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        if (onDeleteAllClicked != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "$uniqueCardsCount / $totalCardsCount",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                IconButton(
                    enabled = !isBusy,
                    onClick = onDeleteAllClicked,
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete all collection cards",
                    )
                }
            }
        }

        if (exportPreview.isNotBlank()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = exportPreview,
                    modifier = Modifier.padding(10.dp),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
fun TradeListAddModeContent(
    isLoading: Boolean,
    error: String?,
    addSearchQuery: String,
    addResults: List<MtgCard>,
    selectedAddCard: MtgCard?,
    addQuantityInput: String,
    addFoil: FoilOption,
    addLanguage: LanguageOption,
    addCondition: CardCondition,
    addPriceInput: String? = null,
    addArtOptions: List<CollectionArtOption>,
    selectedAddArtId: String,
    isEditing: Boolean,
    infoMessage: String,
    onAddSearchChanged: (String) -> Unit,
    onAddSearchSubmitted: () -> Unit,
    onAddResultClicked: (String) -> Unit,
    onQuantityChanged: (String) -> Unit,
    onFoilSelected: (FoilOption) -> Unit,
    onLanguageSelected: (LanguageOption) -> Unit,
    onConditionSelected: (CardCondition) -> Unit,
    onPriceChanged: ((String) -> Unit)? = null,
    onArtSelected: (String) -> Unit,
    onDismissCardParameters: () -> Unit,
    onConfirm: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        TextInputRow(
            query = addSearchQuery,
            onQueryChange = onAddSearchChanged,
            label = "Find card",
            onSearchClick = onAddSearchSubmitted,
        )

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            if (isLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(addResults, key = { it.id }) { card ->
                    AddResultRow(
                        name = card.name,
                        typeLine = card.typeLine,
                        imageUrl = card.imageUrl,
                        onAddClick = { onAddResultClicked(card.id) },
                    )
                }
            }
        }

        selectedAddCard?.let { card ->
            AddCardParametersPanel(
                card = card,
                quantity = addQuantityInput,
                foil = addFoil,
                language = addLanguage,
                condition = addCondition,
                price = addPriceInput,
                artOptions = addArtOptions,
                selectedArtId = selectedAddArtId,
                isEditing = isEditing,
                onQuantityChanged = onQuantityChanged,
                onFoilSelected = onFoilSelected,
                onLanguageSelected = onLanguageSelected,
                onConditionSelected = onConditionSelected,
                onPriceChanged = onPriceChanged,
                onArtSelected = onArtSelected,
                onDismiss = onDismissCardParameters,
                onConfirm = onConfirm,
            )
        }

        if (infoMessage.isNotBlank()) {
            Text(
                text = infoMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
