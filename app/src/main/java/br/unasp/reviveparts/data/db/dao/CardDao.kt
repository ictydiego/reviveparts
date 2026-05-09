package br.unasp.reviveparts.data.db.dao

import androidx.room.*
import br.unasp.reviveparts.data.db.entities.CardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM cards WHERE userId = :uid ORDER BY isDefault DESC, id ASC")
    fun observeForUser(uid: Long): Flow<List<CardEntity>>

    @Insert suspend fun insert(c: CardEntity): Long
    @Update suspend fun update(c: CardEntity)
    @Delete suspend fun delete(c: CardEntity)

    @Query("UPDATE cards SET isDefault = 0 WHERE userId = :uid")
    suspend fun clearDefault(uid: Long)
}
