package br.unasp.reviveparts.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import br.unasp.reviveparts.data.db.dao.*
import br.unasp.reviveparts.data.db.entities.*

@Database(
    entities = [UserEntity::class, ProductEntity::class, CardEntity::class, OrderEntity::class, OrderEventEntity::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun cardDao(): CardDao
    abstract fun orderDao(): OrderDao
    abstract fun orderEventDao(): OrderEventDao
}
