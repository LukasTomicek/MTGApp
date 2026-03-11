package mtg.app.feature.welcome.presentation.welcome

import mtg.app.core.presentation.Direction

sealed interface WelcomeDirection : Direction {
    data object NavigateToMapGuide : WelcomeDirection
}
