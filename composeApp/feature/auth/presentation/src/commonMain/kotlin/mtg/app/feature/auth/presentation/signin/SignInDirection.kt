package mtg.app.feature.auth.presentation.signin

import mtg.app.core.presentation.Direction

sealed interface SignInDirection : Direction {
    data object NavigateToHome : SignInDirection
    data object NavigateToSignUp : SignInDirection
    data object NavigateToForgotPassword : SignInDirection
}
