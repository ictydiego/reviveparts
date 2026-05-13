package br.unasp.reviveparts.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import br.unasp.reviveparts.domain.model.Role

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val password: String,
    val phone: String = "",
    val cpf: String = "",
    val address: String = "",
    val role: Role,
    val firebaseUid: String = ""
)
