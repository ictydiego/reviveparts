package br.unasp.reviveparts.ui.screens.owner.orderdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.db.entities.*
import br.unasp.reviveparts.data.repo.*
import com.google.firebase.firestore.FirebaseFirestoreException
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
    val error = MutableStateFlow<String?>(null)

    init { viewModelScope.launch {
        orders.observeById(orderId).collect { o ->
            order.value = o
            if (o != null) {
                if (product.value == null) product.value = products.findById(o.productId)
                if (customer.value == null) {
                    val embedded = UserEntity(
                        id = 0,
                        name = o.customerName,
                        email = o.customerEmail,
                        password = "",
                        phone = o.customerPhone,
                        address = o.customerAddress,
                        role = br.unasp.reviveparts.domain.model.Role.CUSTOMER,
                        firebaseUid = o.userUid
                    )
                    val needsCloud = embedded.name.isBlank() ||
                        embedded.email.isBlank() ||
                        embedded.phone.isBlank() ||
                        embedded.address.isBlank()
                    customer.value = if (needsCloud) {
                        val cloud = users.findCloudByUid(o.userUid)
                        if (cloud == null) embedded else embedded.copy(
                            name = embedded.name.ifBlank { cloud.name },
                            email = embedded.email.ifBlank { cloud.email },
                            phone = embedded.phone.ifBlank { cloud.phone },
                            address = embedded.address.ifBlank { cloud.address }
                        )
                    } else embedded
                }
            }
        }
    } }

    fun advance() = viewModelScope.launch {
        error.value = null
        try {
            orders.advance(orderId)
        } catch (t: Throwable) {
            error.value = if (t is FirebaseFirestoreException && t.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                "Permissao negada no Firestore. Publique as regras antes de alterar status."
            } else {
                t.message ?: "Nao foi possivel alterar o status"
            }
        }
    }

    companion object {
        fun create(app: RevivePartsApp, id: Long) = OwnerOrderDetailViewModel(app.orderRepo, app.productRepo, app.userRepo, id)
    }
}
