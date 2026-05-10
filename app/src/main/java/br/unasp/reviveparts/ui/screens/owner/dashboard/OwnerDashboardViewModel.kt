package br.unasp.reviveparts.ui.screens.owner.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.db.entities.OrderEntity
import br.unasp.reviveparts.data.db.entities.ProductEntity
import br.unasp.reviveparts.data.repo.OrderRepository
import br.unasp.reviveparts.data.repo.ProductRepository
import br.unasp.reviveparts.domain.model.OrderStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OwnerDashboardViewModel(
    private val orders: OrderRepository,
    products: ProductRepository
) : ViewModel() {
    val selected = MutableStateFlow(OrderStatus.PLACED)
    val list = MutableStateFlow<List<OrderEntity>>(emptyList())
    val all = MutableStateFlow<List<OrderEntity>>(emptyList())
    val productMap = MutableStateFlow<Map<Long, ProductEntity>>(emptyMap())

    init {
        viewModelScope.launch {
            selected.collectLatest { s ->
                orders.observeByStatus(s).collect { list.value = it }
            }
        }
        viewModelScope.launch { orders.observeAll().collect { all.value = it } }
        viewModelScope.launch {
            products.observeAll().collect { l -> productMap.value = l.associateBy { it.id } }
        }
    }

    fun select(s: OrderStatus) { selected.value = s }
    companion object { fun create(app: RevivePartsApp) = OwnerDashboardViewModel(app.orderRepo, app.productRepo) }
}
