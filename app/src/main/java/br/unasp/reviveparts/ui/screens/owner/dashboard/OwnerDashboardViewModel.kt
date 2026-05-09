package br.unasp.reviveparts.ui.screens.owner.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.db.entities.OrderEntity
import br.unasp.reviveparts.data.repo.OrderRepository
import br.unasp.reviveparts.domain.model.OrderStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OwnerDashboardViewModel(private val orders: OrderRepository) : ViewModel() {
    val selected = MutableStateFlow(OrderStatus.PLACED)
    val list = MutableStateFlow<List<OrderEntity>>(emptyList())
    init {
        viewModelScope.launch {
            selected.collect { s -> orders.observeByStatus(s).collect { list.value = it } }
        }
    }
    fun select(s: OrderStatus) { selected.value = s }
    companion object { fun create(app: RevivePartsApp) = OwnerDashboardViewModel(app.orderRepo) }
}
