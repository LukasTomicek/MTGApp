package mtg.app.core.presentation

import mtg.app.core.presentation.state.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<S : Any, E : Event, D : Direction>(initialState: S) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _state = MutableStateFlow(UiState(data = initialState))
    val state: StateFlow<UiState<S>> = _state.asStateFlow()

    private val _direction = MutableSharedFlow<D>(extraBufferCapacity = 1)
    val direction: SharedFlow<D> = _direction.asSharedFlow()

    abstract fun onUiEvent(event: E)

    protected fun updateState(reducer: (S) -> S) {
        _state.value = _state.value.copy(data = reducer(_state.value.data))
    }

    protected fun setLoading(isLoading: Boolean) {
        _state.value = _state.value.copy(isLoading = isLoading)
    }

    protected fun setError(message: String?) {
        _state.value = _state.value.copy(error = message)
    }

    protected fun navigate(direction: D) {
        _direction.tryEmit(direction)
    }

    protected fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        return scope.launch(block = block)
    }

    fun clear() {
        scope.cancel()
    }
}
