package mtg.app.feature.trade.presentation.utils

import mtg.app.feature.trade.presentation.utils.model.CollectionCardEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SellListTransferStore {
    private val _pendingEntries = MutableStateFlow<List<CollectionCardEntry>>(emptyList())
    val pendingEntries: StateFlow<List<CollectionCardEntry>> = _pendingEntries.asStateFlow()

    fun addEntry(entry: CollectionCardEntry) {
        _pendingEntries.update { it + entry }
    }

    fun markConsumed(count: Int) {
        if (count <= 0) return
        _pendingEntries.update { current ->
            if (count >= current.size) emptyList() else current.drop(count)
        }
    }
}
