package mtg.app.feature.trade.presentation.selllist

import mtg.app.core.presentation.Direction

sealed interface SellListDirection : Direction {
    data object None : SellListDirection
}
