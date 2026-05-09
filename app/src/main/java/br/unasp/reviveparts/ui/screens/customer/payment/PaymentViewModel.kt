package br.unasp.reviveparts.ui.screens.customer.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.db.entities.CardEntity
import br.unasp.reviveparts.data.db.entities.OrderEntity
import br.unasp.reviveparts.data.db.entities.ProductEntity
import br.unasp.reviveparts.data.payments.PaymentSimulator
import br.unasp.reviveparts.data.payments.PixGenerator
import br.unasp.reviveparts.data.repo.*
import br.unasp.reviveparts.domain.model.OrderSource
import br.unasp.reviveparts.domain.model.OrderStatus
import br.unasp.reviveparts.domain.model.PaymentType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PaymentViewModel(
    private val products: ProductRepository,
    private val orders: OrderRepository,
    private val cards: CardRepository,
    private val payments: PaymentSimulator,
    private val session: SessionRepository,
    private val productId: Long,
    private val source: OrderSource
) : ViewModel() {

    data class UiState(
        val product: ProductEntity? = null,
        val cards: List<CardEntity> = emptyList(),
        val processing: Boolean = false,
        val error: String? = null,
        val createdOrderId: Long? = null,
        val pixCopyPaste: String? = null
    )
    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private var userId: Long = 0

    init {
        viewModelScope.launch {
            val s = session.current() ?: return@launch
            userId = s.userId
            _state.update { it.copy(product = products.findById(productId)) }
            cards.observeForUser(userId).collect { list -> _state.update { it.copy(cards = list) } }
        }
    }

    fun payCard(numberDigits: String, holder: String, expiry: String, brand: String, save: Boolean) {
        val product = _state.value.product ?: return
        _state.update { it.copy(processing = true, error = null) }
        viewModelScope.launch {
            val r = payments.chargeCard(numberDigits, product.priceCents)
            r.onFailure { _state.update { st -> st.copy(processing = false, error = it.message) } }
                .onSuccess {
                    if (save) cards.add(CardEntity(userId = userId, holderName = holder, last4 = numberDigits.takeLast(4), brand = brand, expiry = expiry), makeDefault = true)
                    val orderId = orders.place(OrderEntity(userId = userId, productId = product.id, status = OrderStatus.PLACED, paymentType = PaymentType.CARD, totalCents = product.priceCents, source = source, createdAt = 0))
                    _state.update { st -> st.copy(processing = false, createdOrderId = orderId) }
                }
        }
    }

    fun preparePix() {
        val product = _state.value.product ?: return
        viewModelScope.launch {
            val orderId = orders.place(OrderEntity(userId = userId, productId = product.id, status = OrderStatus.PLACED, paymentType = PaymentType.PIX, totalCents = product.priceCents, source = source, createdAt = 0))
            _state.update { it.copy(createdOrderId = orderId, pixCopyPaste = PixGenerator.pixCopyPaste(orderId, product.priceCents)) }
        }
    }

    companion object {
        fun create(app: RevivePartsApp, productId: Long, source: OrderSource) =
            PaymentViewModel(app.productRepo, app.orderRepo, app.cardRepo, app.paymentSimulator, app.sessionRepo, productId, source)
    }
}
