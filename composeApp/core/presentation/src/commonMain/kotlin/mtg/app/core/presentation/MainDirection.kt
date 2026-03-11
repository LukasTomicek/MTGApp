package mtg.app.core.presentation

sealed interface MainDirection : Direction {
    data object NavigateToDefault : MainDirection
}
