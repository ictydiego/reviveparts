package br.unasp.reviveparts.ui.screens.owner.orderdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.db.entities.*
import br.unasp.reviveparts.data.repo.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OwnerOrderDetailViewModel(
    private val orders: OrderRepository,
    private val products: ProductRepository,
    private val users: UserRepository,
    private val orderId: Long
) : ViewModel() {
    val order = MutableStateFlow<OrderEntity?>(null)
    val product = MutableStateFlow<ProductEntity?>(null)
    val customer = MutableStateFlow<UserEntity?>(null)

    init { viewModelScope.launch {
        orders.observeById(orderId).collect { o ->
            order.value = o
            if (o != null) {
                if (product.value == null) product.value = products.findById(o.productId)
                if (customer.value == null) customer.value = users.findById(o.userId)
            }
        }
    } }

    fun advance() = viewModelScope.launch { orders.advance(orderId) }

    companion object {
        fun create(app: RevivePartsApp, id: Long) = OwnerOrderDetailViewModel(app.orderRepo, app.productRepo, app.userRepo, id)
    }
}
