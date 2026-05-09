package br.unasp.reviveparts.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val holderName: String,
    val last4: String,
    val brand: String,
    val expiry: String,
    val isDefault: Boolean = false
)
