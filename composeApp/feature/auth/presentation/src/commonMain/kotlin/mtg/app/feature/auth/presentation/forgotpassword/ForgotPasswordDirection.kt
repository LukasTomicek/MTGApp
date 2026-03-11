package mtg.app.feature.auth.presentation.forgotpassword

import mtg.app.core.presentation.Direction

sealed interface ForgotPasswordDirection : Direction {
    data object NavigateToSignIn : ForgotPasswordDirection
}
