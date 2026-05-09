package br.unasp.reviveparts.ui.screens.customer.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.db.entities.ProductEntity
import br.unasp.reviveparts.data.db.entities.UserEntity
import br.unasp.reviveparts.data.repo.ProductRepository
import br.unasp.reviveparts.data.repo.SessionRepository
import br.unasp.reviveparts.data.repo.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    repo: ProductRepository,
    users: UserRepository,
    session: SessionRepository
) : ViewModel() {
    val products: Flow<List<ProductEntity>> = repo.observeReady()
    val user = MutableStateFlow<UserEntity?>(null)

    init {
        viewModelScope.launch {
            session.current()?.let { s ->
                users.observeById(s.userId).collect { user.value = it }
            }
        }
    }

    companion object {
        fun create(app: RevivePartsApp) =
            HomeViewModel(app.productRepo, app.userRepo, app.sessionRepo)
    }
}
