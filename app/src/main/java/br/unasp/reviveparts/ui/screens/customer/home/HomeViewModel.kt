package br.unasp.reviveparts.ui.screens.customer.home

import androidx.lifecycle.ViewModel
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.db.entities.ProductEntity
import br.unasp.reviveparts.data.repo.ProductRepository
import kotlinx.coroutines.flow.Flow

class HomeViewModel(repo: ProductRepository) : ViewModel() {
    val products: Flow<List<ProductEntity>> = repo.observeReady()
    companion object { fun create(app: RevivePartsApp) = HomeViewModel(app.productRepo) }
}
