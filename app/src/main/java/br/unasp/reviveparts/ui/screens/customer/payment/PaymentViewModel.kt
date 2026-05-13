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
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PaymentViewModel(
    private val products: ProductRepository,
    private val users: UserRepository,
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
    private var userUid: String = ""
    private var currentUser: br.unasp.reviveparts.data.db.entities.UserEntity? = null

    init {
        viewModelScope.launch {
            val s = session.current() ?: return@launch
            userId = s.userId
            userUid = s.firebaseUid
            currentUser = users.findById(userId)
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
                    try {
                        if (userUid.isBlank()) error("Sessao Firebase invalida. Entre novamente.")
                        if (save) cards.add(CardEntity(userId = userId, holderName = holder, last4 = numberDigits.takeLast(4), brand = brand, expiry = expiry), makeDefault = true)
                        val orderId = orders.place(orderFor(product, PaymentType.CARD))
                        _state.update { st -> st.copy(processing = false, createdOrderId = orderId) }
                    } catch (t: Throwable) {
                        _state.update { st -> st.copy(processing = false, error = readablePaymentError(t)) }
                    }
                }
        }
    }

    fun preparePix() {
        val product = _state.value.product ?: return
        _state.update { it.copy(processing = true, error = null) }
        viewModelScope.launch {
            try {
                if (userUid.isBlank()) error("Sessao Firebase invalida. Entre novamente.")
                val orderId = orders.place(orderFor(product, PaymentType.PIX))
                _state.update {
                    it.copy(
                        processing = false,
                        createdOrderId = orderId,
                        pixCopyPaste = PixGenerator.pixCopyPaste(orderId, product.priceCents)
                    )
                }
            } catch (t: Throwable) {
                _state.update { it.copy(processing = false, error = readablePaymentError(t)) }
            }
        }
    }

    private fun orderFor(product: ProductEntity, type: PaymentType): OrderEntity {
        val user = currentUser
        return OrderEntity(
            userId = userId,
            productId = product.id,
            status = OrderStatus.PLACED,
            paymentType = type,
            totalCents = product.priceCents,
            source = source,
            createdAt = 0,
            userUid = userUid,
            customerName = user?.name.orEmpty(),
            customerEmail = user?.email.orEmpty(),
            customerPhone = user?.phone.orEmpty(),
            customerAddress = user?.address.orEmpty()
        )
    }

    private fun readablePaymentError(t: Throwable): String =
        if (t is FirebaseFirestoreException && t.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
            "Permissao negada no Firestore. Publique as regras do banco para usuarios autenticados antes de criar pedidos."
        } else {
            t.message ?: "Nao foi possivel criar o pedido"
        }

    companion object {
        fun create(app: RevivePartsApp, productId: Long, source: OrderSource) =
            PaymentViewModel(app.productRepo, app.userRepo, app.orderRepo, app.cardRepo, app.paymentSimulator, app.sessionRepo, productId, source)
    }
}
