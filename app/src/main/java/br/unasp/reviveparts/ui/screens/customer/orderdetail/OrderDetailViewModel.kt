package br.unasp.reviveparts.ui.screens.customer.orderdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.db.entities.OrderEntity
import br.unasp.reviveparts.data.db.entities.ProductEntity
import br.unasp.reviveparts.data.repo.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OrderDetailViewModel(private val orders: OrderRepository, private val products: ProductRepository, private val orderId: Long) : ViewModel() {
    val order = MutableStateFlow<OrderEntity?>(null)
    val product = MutableStateFlow<ProductEntity?>(null)
    init { viewModelScope.launch {
        orders.observeById(orderId).collect { o ->
            order.value = o
            if (o != null && product.value == null) product.value = products.findById(o.productId)
        }
    } }
    companion object { fun create(app: RevivePartsApp, id: Long) = OrderDetailViewModel(app.orderRepo, app.productRepo, id) }
}
