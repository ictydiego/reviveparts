package br.unasp.reviveparts.data.repo

import br.unasp.reviveparts.data.db.dao.OrderDao
import br.unasp.reviveparts.data.db.dao.OrderEventDao
import br.unasp.reviveparts.data.db.entities.OrderEntity
import br.unasp.reviveparts.data.db.entities.OrderEventEntity
import br.unasp.reviveparts.domain.model.OrderStatus

class OrderRepository(
    private val orderDao: OrderDao,
    private val eventDao: OrderEventDao
) {
    fun observeByUser(uid: Long) = orderDao.observeByUser(uid)
    fun observeAll() = orderDao.observeAll()
    fun observeByStatus(s: OrderStatus) = orderDao.observeByStatus(s)
    fun observeById(id: Long) = orderDao.observeById(id)
    fun observeEvents(orderId: Long) = eventDao.observeForOrder(orderId)
    suspend fun findById(id: Long) = orderDao.findById(id)

    suspend fun place(order: OrderEntity): Long {
        val id = orderDao.insert(order.copy(status = OrderStatus.PLACED, createdAt = System.currentTimeMillis()))
        eventDao.insert(OrderEventEntity(orderId = id, status = OrderStatus.PLACED, timestampMs = System.currentTimeMillis()))
        return id
    }

    suspend fun advance(orderId: Long) {
        val current = orderDao.findById(orderId) ?: return
        val next = current.status.next() ?: return
        orderDao.setStatus(orderId, next)
        eventDao.insert(OrderEventEntity(orderId = orderId, status = next, timestampMs = System.currentTimeMillis()))
    }
}
