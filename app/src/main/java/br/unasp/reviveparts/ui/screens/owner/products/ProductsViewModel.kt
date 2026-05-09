package br.unasp.reviveparts.ui.screens.owner.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.db.entities.ProductEntity
import br.unasp.reviveparts.data.repo.ProductRepository
import kotlinx.coroutines.launch

class ProductsViewModel(private val repo: ProductRepository) : ViewModel() {
    val items = repo.observeAll()
    fun delete(p: ProductEntity) = viewModelScope.launch { repo.delete(p) }
    companion object { fun create(app: RevivePartsApp) = ProductsViewModel(app.productRepo) }
}
