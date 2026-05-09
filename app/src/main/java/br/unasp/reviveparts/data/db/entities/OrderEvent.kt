package br.unasp.reviveparts.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import br.unasp.reviveparts.domain.model.OrderStatus

@Entity(tableName = "order_events")
data class OrderEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val status: OrderStatus,
    val timestampMs: Long
)
