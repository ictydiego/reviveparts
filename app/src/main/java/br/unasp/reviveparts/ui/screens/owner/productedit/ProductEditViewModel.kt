package br.unasp.reviveparts.ui.screens.owner.productedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.db.entities.ProductEntity
import br.unasp.reviveparts.data.repo.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ProductEditViewModel(private val repo: ProductRepository, private val id: Long?) : ViewModel() {
    val product = MutableStateFlow(
        ProductEntity(0, "", "", "drawable://placeholder_part", "models/manivela_vw.glb", 0, 0, 0, true)
    )
    init { id?.let { viewModelScope.launch { repo.findById(it)?.let { p -> product.value = p } } } }
    fun update(transform: (ProductEntity) -> ProductEntity) { product.value = transform(product.value) }
    fun save(after: () -> Unit) = viewModelScope.launch { repo.upsert(product.value); after() }
    companion object { fun create(app: RevivePartsApp, id: Long?) = ProductEditViewModel(app.productRepo, id) }
}
