package mtg.app.core.presentation

sealed interface MainUiEvent : Event {
    data object Initialize : MainUiEvent
}
