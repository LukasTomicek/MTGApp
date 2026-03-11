package mtg.app.feature.trade.presentation.collection

import mtg.app.core.presentation.Direction

sealed interface CollectionDirection : Direction {
    data object None : CollectionDirection
}
