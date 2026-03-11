package mtg.app.feature.welcome.presentation.setupprofile

import mtg.app.core.presentation.Direction

sealed interface SetupProfileDirection : Direction {
    data object NavigateBack : SetupProfileDirection
    data object NavigateToHome : SetupProfileDirection
}
