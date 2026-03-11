package mtg.app.core.presentation

class MainViewModel : BaseViewModel<MainScreenState, MainUiEvent, MainDirection>(
    initialState = MainScreenState(),
) {
    override fun onUiEvent(event: MainUiEvent) {
        when (event) {
            MainUiEvent.Initialize -> initialize()
        }
    }

    private fun initialize() {
        updateState { it.copy(initialized = true) }
        navigate(MainDirection.NavigateToDefault)
    }
}
