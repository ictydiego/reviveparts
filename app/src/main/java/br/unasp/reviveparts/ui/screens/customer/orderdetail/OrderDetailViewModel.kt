package br.unasp.reviveparts.ui.screens.customer.orderdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.db.entities.OrderEntity
import br.unasp.reviveparts.data.db.entities.ProductEntity
import br.unasp.reviveparts.data.repo.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OrderDetailViewModel(
    private val orders: OrderRepository,
    private val products: ProductRepository,
    private val orderId: Long,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {
    val order = MutableStateFlow<OrderEntity?>(null)
    val product = MutableStateFlow<ProductEntity?>(null)

    init {
        viewModelScope.launch {
            orders.observeById(orderId).collect { o ->
                order.value = o
                if (o != null && product.value == null) product.value = products.findById(o.productId)
            }
        }
    }

    fun submitReview(rating: Int, comment: String, onSuccess: () -> Unit, onError: () -> Unit) {
        val o = order.value ?: return
        val p = product.value
        viewModelScope.launch {
            runCatching {
                firestore.collection("reviews").add(
                    mapOf(
                        "orderId" to o.id,
                        "productId" to o.productId,
                        "productName" to (p?.name ?: ""),
                        "rating" to rating,
                        "comment" to comment.trim(),
                        "customerName" to o.customerName.ifBlank { auth.currentUser?.email ?: "Anônimo" },
                        "userUid" to o.userUid,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }.onSuccess { onSuccess() }.onFailure { onError() }
        }
    }

    companion object {
        fun create(app: RevivePartsApp, id: Long) = OrderDetailViewModel(
            app.orderRepo, app.productRepo, id, app.firestore, app.firebaseAuth
        )
    }
}
