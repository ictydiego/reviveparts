package br.unasp.reviveparts.ui.screens.customer.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.db.entities.ProductEntity
import br.unasp.reviveparts.data.repo.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CartViewModel(private val products: ProductRepository, private val productId: Long) : ViewModel() {
    val product = MutableStateFlow<ProductEntity?>(null)
    init { viewModelScope.launch { product.value = products.findById(productId) } }
    companion object { fun create(app: RevivePartsApp, id: Long) = CartViewModel(app.productRepo, id) }
}
