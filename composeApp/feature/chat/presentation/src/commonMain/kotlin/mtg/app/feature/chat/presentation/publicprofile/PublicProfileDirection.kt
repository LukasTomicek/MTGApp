package mtg.app.feature.chat.presentation.publicprofile

import mtg.app.core.presentation.Direction

sealed interface PublicProfileDirection : Direction {
    data object None : PublicProfileDirection
}
