package br.unasp.reviveparts.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val photoPath: String,
    val model3dAsset: String,
    val priceCents: Long,
    val prototypeHours: Int,
    val stockQty: Int,
    val isReady: Boolean
)
