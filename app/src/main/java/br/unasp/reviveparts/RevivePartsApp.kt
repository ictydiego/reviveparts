package br.unasp.reviveparts

import android.app.Application
import androidx.room.Room
import br.unasp.reviveparts.data.db.AppDatabase
import br.unasp.reviveparts.data.db.Seed
import br.unasp.reviveparts.data.repo.*
import br.unasp.reviveparts.data.ai.FakeAiService
import br.unasp.reviveparts.data.payments.PaymentSimulator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class RevivePartsApp : Application() {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val db by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "reviveparts.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    val userRepo by lazy { UserRepository(db.userDao()) }
    val productRepo by lazy { ProductRepository(db.productDao()) }
    val orderRepo by lazy { OrderRepository(db.orderDao(), db.orderEventDao()) }
    val cardRepo by lazy { CardRepository(db.cardDao()) }
    val sessionRepo by lazy { SessionRepository(this) }
    val aiService by lazy { FakeAiService() }
    val paymentSimulator by lazy { PaymentSimulator() }

    override fun onCreate() {
        super.onCreate()
        appScope.launch { Seed.seedIfEmpty(db) }
    }
}

val android.content.Context.app: RevivePartsApp
    get() = applicationContext as RevivePartsApp
