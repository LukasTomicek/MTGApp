package mtg.app.feature.trade.presentation.utils.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import mtg.app.core.presentation.components.CardImage
import mtg.app.core.presentation.theme.AppTheme
import mtg.app.core.presentation.utils.formatEuroPrice
import mtg.app.feature.trade.domain.MarketPlaceCard

@Composable
fun MarketCardColumn(
    card: MarketPlaceCard,
    onClick: (() -> Unit)?,
    pricePrefix: String = "From",
) {
    Box(
        modifier = Modifier
            .width(143.dp)
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
            ),
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            CardImage(
                imageUrl = card.imageUrl,
                heightDp = 170
            )

            Text(
                text = card.cardName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$pricePrefix ${formatEuroPrice(card.fromPrice)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
