package mtg.app.feature.auth.presentation.signup

import mtg.app.core.presentation.Direction

sealed interface SignUpDirection : Direction {
    data object NavigateToHome : SignUpDirection
    data object NavigateToSignIn : SignUpDirection
}
