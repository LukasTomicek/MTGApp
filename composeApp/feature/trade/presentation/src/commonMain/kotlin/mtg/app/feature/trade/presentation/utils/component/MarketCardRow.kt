package mtg.app.feature.trade.presentation.utils.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mtg.app.core.presentation.components.CardImage
import mtg.app.core.presentation.theme.AppTheme
import mtg.app.feature.trade.domain.MarketPlaceCard

@Composable
fun MarketCardRow(
    card: MarketPlaceCard,
    onClick: (() -> Unit)?,
    pricePrefix: String = "From",
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = AppTheme.shapes.small)
            .background(color = AppTheme.colors.gray200)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .border(
                width = 1.dp,
                color = AppTheme.colors.black,
                shape = AppTheme.shapes.small
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CardImage(imageUrl = card.imageUrl, heightDp = 72)

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = card.cardName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = card.cardTypeLine,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Text(
                text = "$pricePrefix ${card.fromPrice?.let { "$$it" } ?: "-"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
