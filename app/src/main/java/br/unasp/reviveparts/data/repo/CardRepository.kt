package br.unasp.reviveparts.data.repo

import br.unasp.reviveparts.data.db.dao.CardDao
import br.unasp.reviveparts.data.db.entities.CardEntity

class CardRepository(private val dao: CardDao) {
    fun observeForUser(uid: Long) = dao.observeForUser(uid)
    suspend fun add(c: CardEntity, makeDefault: Boolean): Long {
        if (makeDefault) dao.clearDefault(c.userId)
        return dao.insert(c.copy(isDefault = makeDefault))
    }
    suspend fun delete(c: CardEntity) = dao.delete(c)
    suspend fun setDefault(c: CardEntity) {
        dao.clearDefault(c.userId)
        dao.update(c.copy(isDefault = true))
    }
}
