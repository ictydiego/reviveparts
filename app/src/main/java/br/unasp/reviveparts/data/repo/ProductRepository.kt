package br.unasp.reviveparts.data.repo

import br.unasp.reviveparts.data.db.dao.ProductDao
import br.unasp.reviveparts.data.db.entities.ProductEntity

class ProductRepository(private val dao: ProductDao) {
    fun observeReady() = dao.observeReady()
    fun observeAll() = dao.observeAll()
    suspend fun findById(id: Long) = dao.findById(id)
    suspend fun upsert(p: ProductEntity): Long = if (p.id == 0L) dao.insert(p) else { dao.update(p); p.id }
    suspend fun delete(p: ProductEntity) = dao.delete(p)
}
