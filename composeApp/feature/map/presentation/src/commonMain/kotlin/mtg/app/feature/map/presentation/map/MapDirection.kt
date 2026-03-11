package mtg.app.feature.map.presentation.map

import mtg.app.core.presentation.Direction

sealed interface MapDirection : Direction {
    data object None : MapDirection
}
