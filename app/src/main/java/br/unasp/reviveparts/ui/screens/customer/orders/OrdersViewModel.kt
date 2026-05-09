package br.unasp.reviveparts.ui.screens.customer.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.db.entities.OrderEntity
import br.unasp.reviveparts.data.repo.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OrdersViewModel(orders: OrderRepository, session: SessionRepository) : ViewModel() {
    val items = MutableStateFlow<List<OrderEntity>>(emptyList())
    init {
        viewModelScope.launch {
            val s = session.current() ?: return@launch
            orders.observeByUser(s.userId).collect { items.value = it }
        }
    }
    companion object { fun create(app: RevivePartsApp) = OrdersViewModel(app.orderRepo, app.sessionRepo) }
}
