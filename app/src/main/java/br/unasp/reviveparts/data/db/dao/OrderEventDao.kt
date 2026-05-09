package br.unasp.reviveparts.data.db.dao

import androidx.room.*
import br.unasp.reviveparts.data.db.entities.OrderEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderEventDao {
    @Query("SELECT * FROM order_events WHERE orderId = :oid ORDER BY timestampMs ASC")
    fun observeForOrder(oid: Long): Flow<List<OrderEventEntity>>

    @Insert suspend fun insert(e: OrderEventEntity): Long
}
