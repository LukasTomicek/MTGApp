package mtg.app.feature.welcome.presentation.welcome

import mtg.app.core.presentation.BaseViewModel

class WelcomeViewModel() : BaseViewModel<WelcomeScreenState, WelcomeUiEvent, WelcomeDirection>(
    initialState = WelcomeScreenState(),
) {
    init {

    }

    override fun onUiEvent(event: WelcomeUiEvent) {
        when (event) {
            WelcomeUiEvent.ContinueClicked -> navigate(WelcomeDirection.NavigateToMapGuide)
        }
    }
}
