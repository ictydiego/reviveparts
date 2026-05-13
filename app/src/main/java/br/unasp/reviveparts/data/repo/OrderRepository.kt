package br.unasp.reviveparts.data.repo

import br.unasp.reviveparts.data.db.dao.OrderDao
import br.unasp.reviveparts.data.db.dao.OrderEventDao
import br.unasp.reviveparts.data.db.entities.OrderEntity
import br.unasp.reviveparts.data.db.entities.OrderEventEntity
import br.unasp.reviveparts.domain.model.OrderSource
import br.unasp.reviveparts.domain.model.OrderStatus
import br.unasp.reviveparts.domain.model.PaymentType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class OrderRepository(
    private val orderDao: OrderDao,
    private val eventDao: OrderEventDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val ordersCollection get() = firestore.collection("orders")

    fun observeByUser(uid: Long, firebaseUid: String): Flow<List<OrderEntity>> =
        if (shouldUseCloud(firebaseUid)) {
            observeCloudOrders { listener ->
                ordersCollection.whereEqualTo("userUid", firebaseUid)
                    .addSnapshotListener { snap, error -> listener(snap, error) }
            }
        } else {
            orderDao.observeByUser(uid)
        }

    fun observeAll(): Flow<List<OrderEntity>> =
        if (shouldUseCloud()) {
            observeCloudOrders { listener ->
                ordersCollection.addSnapshotListener { snap, error -> listener(snap, error) }
            }
        } else {
            orderDao.observeAll()
        }

    fun observeByStatus(s: OrderStatus): Flow<List<OrderEntity>> =
        if (shouldUseCloud()) {
            observeCloudOrders { listener ->
                ordersCollection.whereEqualTo("status", s.name)
                    .addSnapshotListener { snap, error -> listener(snap, error) }
            }
        } else {
            orderDao.observeByStatus(s)
        }

    fun observeById(id: Long): Flow<OrderEntity?> =
        if (shouldUseCloud()) {
            callbackFlow {
                val registration = ordersCollection.document(id.toString()).addSnapshotListener { snap, error ->
                    if (error != null) {
                        trySend(null)
                        return@addSnapshotListener
                    }
                    trySend(snap?.toOrderEntity())
                }
                awaitClose { registration.remove() }
            }
        } else {
            orderDao.observeById(id)
        }

    fun observeEvents(orderId: Long): Flow<List<OrderEventEntity>> =
        if (shouldUseCloud()) {
            callbackFlow {
                val registration = ordersCollection.document(orderId.toString())
                    .collection("events")
                    .addSnapshotListener { snap, error ->
                        if (error != null) {
                            trySend(emptyList())
                            return@addSnapshotListener
                        }
                        trySend(
                            snap.orEmptyDocuments()
                                .mapNotNull { it.toOrderEventEntity(orderId) }
                                .sortedBy { it.timestampMs }
                        )
                    }
                awaitClose { registration.remove() }
            }
        } else {
            eventDao.observeForOrder(orderId)
        }

    suspend fun findById(id: Long): OrderEntity? =
        if (shouldUseCloud()) {
            runCatching {
                ordersCollection.document(id.toString()).get().await().toOrderEntity()
            }.getOrNull() ?: orderDao.findById(id)
        } else {
            orderDao.findById(id)
        }

    suspend fun place(order: OrderEntity): Long {
        val now = System.currentTimeMillis()
        return if (shouldUseCloud(order.userUid)) {
            val id = nextCloudOrderId()
            val saved = order.copy(id = id, status = OrderStatus.PLACED, createdAt = now)
            val event = OrderEventEntity(orderId = id, status = OrderStatus.PLACED, timestampMs = now)
            val orderRef = ordersCollection.document(id.toString())
            val eventRef = orderRef.collection("events").document()
            firestore.batch()
                .set(orderRef, saved.toCloudMap(), SetOptions.merge())
                .set(eventRef, event.toCloudMap())
                .commit()
                .await()
            id
        } else {
            val id = orderDao.insert(order.copy(status = OrderStatus.PLACED, createdAt = now))
            eventDao.insert(OrderEventEntity(orderId = id, status = OrderStatus.PLACED, timestampMs = now))
            id
        }
    }

    suspend fun advance(orderId: Long) {
        if (shouldUseCloud()) {
            val orderRef = ordersCollection.document(orderId.toString())
            firestore.runTransaction { tx ->
                val snap = tx.get(orderRef)
                val current = snap.getString("status")?.toOrderStatusOrNull() ?: return@runTransaction false
                val next = current.next() ?: return@runTransaction false
                val now = System.currentTimeMillis()
                tx.update(
                    orderRef,
                    mapOf(
                        "status" to next.name,
                        "updatedAt" to now
                    )
                )
                tx.set(
                    orderRef.collection("events").document(),
                    OrderEventEntity(orderId = orderId, status = next, timestampMs = now).toCloudMap()
                )
                true
            }.await()
        } else {
            val current = orderDao.findById(orderId) ?: return
            val next = current.status.next() ?: return
            orderDao.setStatus(orderId, next)
            eventDao.insert(OrderEventEntity(orderId = orderId, status = next, timestampMs = System.currentTimeMillis()))
        }
    }

    private fun observeCloudOrders(
        listen: ((QuerySnapshot?, Exception?) -> Unit) -> com.google.firebase.firestore.ListenerRegistration
    ): Flow<List<OrderEntity>> = callbackFlow {
        val registration = listen { snap, error ->
            if (error != null) {
                trySend(emptyList())
                return@listen
            }
            trySend(
                snap.orEmptyDocuments()
                    .mapNotNull { it.toOrderEntity() }
                    .sortedByDescending { it.createdAt }
            )
        }
        awaitClose { registration.remove() }
    }

    private suspend fun nextCloudOrderId(): Long {
        val counterRef = firestore.collection("metadata").document("counters")
        return firestore.runTransaction { tx ->
            val current = tx.get(counterRef).getLong("orderSeq") ?: 0L
            val next = current + 1L
            tx.set(counterRef, mapOf("orderSeq" to next), SetOptions.merge())
            next
        }.await()
    }

    private fun shouldUseCloud(requiredUid: String = ""): Boolean {
        val currentUid = auth.currentUser?.uid ?: return false
        return requiredUid.isBlank() || currentUid == requiredUid
    }

    private fun DocumentSnapshot.toOrderEntity(): OrderEntity? {
        val orderId = getLong("id") ?: id.toLongOrNull() ?: return null
        val productId = getLong("productId") ?: return null
        val status = getString("status")?.toOrderStatusOrNull() ?: OrderStatus.PLACED
        val paymentType = getString("paymentType")?.toPaymentTypeOrNull() ?: PaymentType.PIX
        val source = getString("source")?.toOrderSourceOrNull() ?: OrderSource.CATALOG

        return OrderEntity(
            id = orderId,
            userId = getLong("userId") ?: 0L,
            productId = productId,
            status = status,
            paymentType = paymentType,
            totalCents = getLong("totalCents") ?: 0L,
            source = source,
            createdAt = getLong("createdAt") ?: 0L,
            userUid = getString("userUid").orEmpty(),
            customerName = getString("customerName").orEmpty(),
            customerEmail = getString("customerEmail").orEmpty(),
            customerPhone = getString("customerPhone").orEmpty(),
            customerAddress = getString("customerAddress").orEmpty()
        )
    }

    private fun DocumentSnapshot.toOrderEventEntity(orderId: Long): OrderEventEntity? {
        val status = getString("status")?.toOrderStatusOrNull() ?: return null
        return OrderEventEntity(
            id = getLong("id") ?: 0L,
            orderId = getLong("orderId") ?: orderId,
            status = status,
            timestampMs = getLong("timestampMs") ?: 0L
        )
    }

    private fun OrderEntity.toCloudMap(): Map<String, Any> = mapOf(
        "id" to id,
        "userId" to userId,
        "userUid" to userUid,
        "productId" to productId,
        "status" to status.name,
        "paymentType" to paymentType.name,
        "totalCents" to totalCents,
        "source" to source.name,
        "createdAt" to createdAt,
        "updatedAt" to System.currentTimeMillis(),
        "customerName" to customerName,
        "customerEmail" to customerEmail,
        "customerPhone" to customerPhone,
        "customerAddress" to customerAddress
    )

    private fun OrderEventEntity.toCloudMap(): Map<String, Any> = mapOf(
        "orderId" to orderId,
        "status" to status.name,
        "timestampMs" to timestampMs
    )

    private fun QuerySnapshot?.orEmptyDocuments(): List<DocumentSnapshot> =
        this?.documents.orEmpty()

    private fun String.toOrderStatusOrNull(): OrderStatus? =
        runCatching { OrderStatus.valueOf(this) }.getOrNull()

    private fun String.toPaymentTypeOrNull(): PaymentType? =
        runCatching { PaymentType.valueOf(this) }.getOrNull()

    private fun String.toOrderSourceOrNull(): OrderSource? =
        runCatching { OrderSource.valueOf(this) }.getOrNull()
}
