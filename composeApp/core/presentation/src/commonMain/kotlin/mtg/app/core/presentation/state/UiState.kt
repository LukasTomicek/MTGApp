package mtg.app.core.presentation.state

data class UiState<T>(
    val data: T,
    val isLoading: Boolean = false,
    val error: String? = null,
)
