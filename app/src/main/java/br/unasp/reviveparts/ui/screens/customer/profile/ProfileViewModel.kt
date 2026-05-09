package br.unasp.reviveparts.ui.screens.customer.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.db.entities.UserEntity
import br.unasp.reviveparts.data.db.entities.CardEntity
import br.unasp.reviveparts.data.repo.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val users: UserRepository,
    private val cards: CardRepository,
    private val session: SessionRepository
) : ViewModel() {
    val user = MutableStateFlow<UserEntity?>(null)
    val cardsList = MutableStateFlow<List<CardEntity>>(emptyList())
    private var uid: Long = 0

    init { viewModelScope.launch {
        val s = session.current() ?: return@launch; uid = s.userId
        users.observeById(uid).collect { user.value = it }
    } }
    init { viewModelScope.launch {
        val s = session.current() ?: return@launch
        cards.observeForUser(s.userId).collect { cardsList.value = it }
    } }

    fun save(name: String, phone: String, address: String) = viewModelScope.launch {
        val u = user.value ?: return@launch
        users.update(u.copy(name = name, phone = phone, address = address))
    }
    fun logout(after: () -> Unit) = viewModelScope.launch { session.logout(); after() }
    fun deleteCard(c: CardEntity) = viewModelScope.launch { cards.delete(c) }

    companion object {
        fun create(app: RevivePartsApp) = ProfileViewModel(app.userRepo, app.cardRepo, app.sessionRepo)
    }
}
