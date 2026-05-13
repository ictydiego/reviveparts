package br.unasp.reviveparts.ui.screens.customer.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.db.entities.ProductEntity
import br.unasp.reviveparts.data.db.entities.UserEntity
import br.unasp.reviveparts.data.repo.OrderRepository
import br.unasp.reviveparts.data.repo.ProductRepository
import br.unasp.reviveparts.data.repo.SessionRepository
import br.unasp.reviveparts.data.repo.UserRepository
import br.unasp.reviveparts.domain.model.OrderStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    repo: ProductRepository,
    orders: OrderRepository,
    users: UserRepository,
    session: SessionRepository
) : ViewModel() {
    val products: Flow<List<ProductEntity>> = repo.observeReady()
    val user = MutableStateFlow<UserEntity?>(null)
    val hasOrderUpdate = MutableStateFlow(false)
    private var knownOrderStatuses: Map<Long, OrderStatus>? = null

    init {
        viewModelScope.launch {
            session.current()?.let { s ->
                users.observeById(s.userId).collect { user.value = it }
            }
        }
        viewModelScope.launch {
            session.current()?.let { s ->
                orders.observeByUser(s.userId, s.firebaseUid).collect { list ->
                    val current = list.associate { it.id to it.status }
                    val previous = knownOrderStatuses
                    if (previous != null && current.any { (id, status) -> previous[id] != null && previous[id] != status }) {
                        hasOrderUpdate.value = true
                    }
                    knownOrderStatuses = current
                }
            }
        }
    }

    fun clearOrderUpdate() {
        hasOrderUpdate.value = false
    }

    companion object {
        fun create(app: RevivePartsApp) =
            HomeViewModel(app.productRepo, app.orderRepo, app.userRepo, app.sessionRepo)
    }
}
