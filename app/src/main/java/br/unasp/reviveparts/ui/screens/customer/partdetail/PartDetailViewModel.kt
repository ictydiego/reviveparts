package br.unasp.reviveparts.ui.screens.customer.partdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.db.entities.ProductEntity
import br.unasp.reviveparts.data.repo.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class PartDetailViewModel(private val repo: ProductRepository, private val id: Long) : ViewModel() {
    val product = MutableStateFlow<ProductEntity?>(null)
    init { viewModelScope.launch { product.value = repo.findById(id) } }
    companion object {
        fun create(app: RevivePartsApp, id: Long) = PartDetailViewModel(app.productRepo, id)
    }
}
