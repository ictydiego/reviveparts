package br.unasp.reviveparts.ui.screens.customer.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.db.entities.ProductEntity
import br.unasp.reviveparts.data.repo.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel(products: ProductRepository) : ViewModel() {
    private val all = MutableStateFlow<List<ProductEntity>>(emptyList())
    val favIds = MutableStateFlow(setOf<Long>(2L, 5L))
    val items = MutableStateFlow<List<ProductEntity>>(emptyList())

    init {
        viewModelScope.launch {
            products.observeAll().collect { list ->
                all.value = list
                items.value = list.filter { it.id in favIds.value }
            }
        }
    }

    fun toggle(id: Long) {
        favIds.value = if (id in favIds.value) favIds.value - id else favIds.value + id
        items.value = all.value.filter { it.id in favIds.value }
    }

    companion object { fun create(app: RevivePartsApp) = FavoritesViewModel(app.productRepo) }
}
