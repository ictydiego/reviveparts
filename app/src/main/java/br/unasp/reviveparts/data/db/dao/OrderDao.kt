package br.unasp.reviveparts.data.db.dao

import androidx.room.*
import br.unasp.reviveparts.data.db.entities.OrderEntity
import br.unasp.reviveparts.domain.model.OrderStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders WHERE userId = :uid ORDER BY createdAt DESC")
    fun observeByUser(uid: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE status = :s ORDER BY createdAt DESC")
    fun observeByStatus(s: OrderStatus): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id")
    fun observeById(id: Long): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): OrderEntity?

    @Insert suspend fun insert(o: OrderEntity): Long

    @Query("UPDATE orders SET status = :s WHERE id = :id")
    suspend fun setStatus(id: Long, s: OrderStatus)
}
