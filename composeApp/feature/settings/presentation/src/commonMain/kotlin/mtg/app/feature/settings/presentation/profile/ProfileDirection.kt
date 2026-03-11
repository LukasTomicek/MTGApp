package mtg.app.feature.settings.presentation.profile

import mtg.app.core.presentation.Direction

sealed interface ProfileDirection : Direction {
    data object None : ProfileDirection
}
