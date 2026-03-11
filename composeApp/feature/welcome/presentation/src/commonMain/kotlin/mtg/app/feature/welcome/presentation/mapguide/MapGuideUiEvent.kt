package mtg.app.feature.welcome.presentation.mapguide

import mtg.app.core.presentation.Event
import mtg.app.feature.welcome.presentation.WelcomeLocation

sealed interface MapGuideUiEvent : Event {
    data object ContinueClicked : MapGuideUiEvent
    data object BackClicked : MapGuideUiEvent
    data object SetCurrentLocationAsDefaultPinClicked : MapGuideUiEvent
    data class CurrentLocationResolved(val location: WelcomeLocation?) : MapGuideUiEvent
}
