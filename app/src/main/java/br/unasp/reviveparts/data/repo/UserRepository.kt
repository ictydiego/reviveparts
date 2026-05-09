package br.unasp.reviveparts.data.repo

import br.unasp.reviveparts.data.db.dao.UserDao
import br.unasp.reviveparts.data.db.entities.UserEntity
import br.unasp.reviveparts.domain.model.Role

class UserRepository(private val dao: UserDao) {
    private val ownerEmail = "dono@reviveparts.com"

    suspend fun login(email: String, password: String): Result<UserEntity> {
        val u = dao.findByEmail(email.trim().lowercase())
            ?: return Result.failure(IllegalArgumentException("E-mail não encontrado"))
        if (u.password != password) return Result.failure(IllegalArgumentException("Senha incorreta"))
        return Result.success(u)
    }

    suspend fun register(name: String, email: String, password: String, phone: String, cpf: String, address: String): Result<UserEntity> {
        val normEmail = email.trim().lowercase()
        if (dao.findByEmail(normEmail) != null) return Result.failure(IllegalStateException("E-mail já cadastrado"))
        val role = if (normEmail == ownerEmail) Role.OWNER else Role.CUSTOMER
        val id = dao.insert(UserEntity(name = name, email = normEmail, password = password, phone = phone, cpf = cpf, address = address, role = role))
        return Result.success(dao.findById(id)!!)
    }

    suspend fun findById(id: Long) = dao.findById(id)
    fun observeById(id: Long) = dao.observeById(id)
    suspend fun update(u: UserEntity) = dao.update(u)
}
