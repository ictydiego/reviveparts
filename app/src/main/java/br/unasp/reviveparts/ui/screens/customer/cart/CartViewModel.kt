package br.unasp.reviveparts.ui.screens.customer.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.db.entities.ProductEntity
import br.unasp.reviveparts.data.db.entities.UserEntity
import br.unasp.reviveparts.data.repo.ProductRepository
import br.unasp.reviveparts.data.repo.SessionRepository
import br.unasp.reviveparts.data.repo.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CartViewModel(
    private val products: ProductRepository,
    private val users: UserRepository,
    private val session: SessionRepository,
    private val productId: Long
) : ViewModel() {
    val product = MutableStateFlow<ProductEntity?>(null)
    val qty = MutableStateFlow(1)
    val others = MutableStateFlow<List<ProductEntity>>(emptyList())
    val user = MutableStateFlow<UserEntity?>(null)

    init {
        viewModelScope.launch { product.value = products.findById(productId) }
        viewModelScope.launch {
            products.observeReady().collect { all ->
                others.value = all.filter { it.id != productId }.take(3)
            }
        }
        viewModelScope.launch {
            session.current()?.let { s ->
                users.observeById(s.userId).collect { user.value = it }
            }
        }
    }

    fun changeQty(delta: Int) { qty.value = (qty.value + delta).coerceAtLeast(1) }
    fun setQty(v: Int) { qty.value = v.coerceAtLeast(0) }

    fun totalCents(): Long {
        val p = product.value ?: return 0L
        return p.priceCents * qty.value
    }

    companion object {
        fun create(app: RevivePartsApp, id: Long) =
            CartViewModel(app.productRepo, app.userRepo, app.sessionRepo, id)
    }
}
