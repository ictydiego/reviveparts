package br.unasp.reviveparts.ui.screens.customer.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.db.entities.OrderEntity
import br.unasp.reviveparts.data.db.entities.ProductEntity
import br.unasp.reviveparts.data.repo.OrderRepository
import br.unasp.reviveparts.data.repo.ProductRepository
import br.unasp.reviveparts.data.repo.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class OrdersViewModel(
    orders: OrderRepository,
    productsRepo: ProductRepository,
    session: SessionRepository
) : ViewModel() {
    val items = MutableStateFlow<List<OrderEntity>>(emptyList())
    val products = MutableStateFlow<Map<Long, ProductEntity>>(emptyMap())

    init {
        viewModelScope.launch {
            val s = session.current() ?: return@launch
            orders.observeByUser(s.userId, s.firebaseUid).collect { items.value = it }
        }
        viewModelScope.launch {
            productsRepo.observeAll().collect { list -> products.value = list.associateBy { it.id } }
        }
    }

    companion object {
        fun create(app: RevivePartsApp) = OrdersViewModel(app.orderRepo, app.productRepo, app.sessionRepo)
    }
}
