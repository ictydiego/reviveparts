package br.unasp.reviveparts.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import br.unasp.reviveparts.domain.model.OrderSource
import br.unasp.reviveparts.domain.model.OrderStatus
import br.unasp.reviveparts.domain.model.PaymentType

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val productId: Long,
    val status: OrderStatus,
    val paymentType: PaymentType,
    val totalCents: Long,
    val source: OrderSource,
    val createdAt: Long,
    val userUid: String = "",
    val customerName: String = "",
    val customerEmail: String = "",
    val customerPhone: String = "",
    val customerAddress: String = ""
)
