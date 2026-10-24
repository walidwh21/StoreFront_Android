package com.storefront.app.feature.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storefront.app.core.util.attempt
import com.storefront.app.core.util.userMessage
import com.storefront.app.data.CatalogRepository
import com.storefront.app.domain.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val repository: CatalogRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CatalogUiState())
    val state: StateFlow<CatalogUiState> = _state.asStateFlow()

    init {
        dispatch(CatalogAction.Refresh)
    }

    fun dispatch(action: CatalogAction) {
        when (action) {
            CatalogAction.Refresh -> refresh()
            is CatalogAction.Open -> open(action.productId)
            is CatalogAction.Restock -> restock(action.productId, action.amount)
            is CatalogAction.MarkSoldOut -> markSoldOut(action.productId)
        }
    }

    private fun refresh() = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null) }
        attempt { repository.fetchAll() }
            .onSuccess { list -> _state.update { it.copy(loading = false, products = list) } }
            .onFailure { e ->
                _state.update { it.copy(loading = false, error = e.userMessage("Could not load products.")) }
            }
    }

    private fun open(productId: String) = viewModelScope.launch {
        // The cached copy (if any) is shown immediately; this just freshens it.
        attempt { repository.fetchById(productId) }
            .onSuccess { fresh -> if (fresh != null) upsert(fresh) }
            .onFailure { e ->
                if (_state.value.byId(productId) == null) {
                    _state.update { it.copy(error = e.userMessage("Could not load this product.")) }
                }
            }
    }

    private fun restock(productId: String, amount: Int) = viewModelScope.launch {
        val now = Date()
        attempt { repository.restock(productId, amount, now) }
            .onSuccess {
                patch(productId) { it.copy(stock = it.stock + amount, restockDate = now) }
            }
            .onFailure { e -> _state.update { it.copy(error = e.userMessage("Restock failed.")) } }
    }

    private fun markSoldOut(productId: String) = viewModelScope.launch {
        val now = Date()
        attempt { repository.markSoldOut(productId, now) }
            .onSuccess { patch(productId) { it.copy(stock = 0, restockDate = now) } }
            .onFailure { e -> _state.update { it.copy(error = e.userMessage("Update failed.")) } }
    }

    private fun upsert(product: Product) = _state.update { current ->
        val exists = current.products.any { it.id == product.id }
        current.copy(
            products = if (exists) current.products.map { if (it.id == product.id) product else it }
            else current.products + product,
            error = null
        )
    }

    private fun patch(productId: String, change: (Product) -> Product) = _state.update { current ->
        current.copy(
            products = current.products.map { if (it.id == productId) change(it) else it },
            error = null
        )
    }
}
