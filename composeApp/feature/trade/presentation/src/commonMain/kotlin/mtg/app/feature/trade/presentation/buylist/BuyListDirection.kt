package mtg.app.feature.trade.presentation.buylist

import mtg.app.core.presentation.Direction

sealed interface BuyListDirection : Direction {
    data object None : BuyListDirection
}
