package mtg.app.feature.trade.presentation.utils.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import mtg.app.core.presentation.components.AppButton
import mtg.app.core.presentation.components.AppButtonState
import mtg.app.core.presentation.components.CardImage
import mtg.app.core.presentation.components.TextState
import mtg.app.core.presentation.components.appOutlinedTextFieldColors
import mtg.app.feature.trade.domain.MtgCard
import mtg.app.feature.trade.presentation.utils.model.CardCondition
import mtg.app.feature.trade.presentation.utils.model.CollectionArtOption
import mtg.app.feature.trade.presentation.utils.model.FoilOption
import mtg.app.feature.trade.presentation.utils.model.LanguageOption

@Composable
fun AddCardParametersPanel(
    card: MtgCard,
    quantity: String,
    foil: FoilOption,
    language: LanguageOption,
    condition: CardCondition,
    artOptions: List<CollectionArtOption>,
    selectedArtId: String,
    isEditing: Boolean,
    onQuantityChanged: (String) -> Unit,
    onFoilSelected: (FoilOption) -> Unit,
    onLanguageSelected: (LanguageOption) -> Unit,
    onConditionSelected: (CardCondition) -> Unit,
    onArtSelected: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    price: String? = null,
    onPriceChanged: ((String) -> Unit)? = null,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CardImage(
                        imageUrl = card.imageUrl,
                        heightDp = 46,
                    )
                    Text(
                        text = if (isEditing) "Edit ${card.name}" else "Add ${card.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                        )
                    }
                }

                OutlinedTextField(
                    value = quantity,
                    onValueChange = onQuantityChanged,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Count") },
                    colors = appOutlinedTextFieldColors(),
                )

                if (price != null && onPriceChanged != null) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = onPriceChanged,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Price") },
                        colors = appOutlinedTextFieldColors(),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ExpandableSelector(
                        modifier = Modifier.weight(1f),
                        label = "Foil",
                        currentId = foil.name,
                        options = FoilOption.entries.map { ExpandableSelectorOption(id = it.name, label = it.label) },
                        onSelect = { id -> onFoilSelected(FoilOption.entries.first { it.name == id }) },
                    )

                    ExpandableSelector(
                        modifier = Modifier.weight(1f),
                        label = "Language",
                        currentId = language.name,
                        options = LanguageOption.entries.map { ExpandableSelectorOption(id = it.name, label = it.label) },
                        onSelect = { id -> onLanguageSelected(LanguageOption.entries.first { it.name == id }) },
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ExpandableSelector(
                        modifier = Modifier.weight(1f),
                        label = "Quality",
                        currentId = condition.name,
                        options = CardCondition.entries.map { ExpandableSelectorOption(id = it.name, label = it.label) },
                        onSelect = { id -> onConditionSelected(CardCondition.entries.first { it.name == id }) },
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ExpandableSelector(
                        modifier = Modifier.fillMaxWidth(),
                        label = "Art",
                        currentId = selectedArtId,
                        options = artOptions.map { ExpandableSelectorOption(id = it.id, label = it.label, imageUrl = it.imageUrl) },
                        largeImageMode = true,
                        onSelect = onArtSelected,
                    )
                }

                AppButton(
                    modifier = Modifier.fillMaxWidth(),
                    state = AppButtonState(
                        title = TextState(if (isEditing) "Ulozit" else "Pridat"),
                        onClick = onConfirm,
                    ),
                )
            }
        }
    }
}
