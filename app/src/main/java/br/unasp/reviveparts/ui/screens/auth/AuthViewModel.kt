package br.unasp.reviveparts.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.repo.SessionRepository
import br.unasp.reviveparts.data.repo.UserRepository
import br.unasp.reviveparts.domain.model.Role
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val users: UserRepository,
    private val session: SessionRepository
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val error: String? = null,
        val loggedInRole: Role? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    fun login(email: String, password: String) {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            val r = users.login(email, password)
            r.onSuccess {
                session.login(it.id, it.role)
                _state.value = UiState(loggedInRole = it.role)
            }.onFailure {
                _state.value = UiState(error = it.message)
            }
        }
    }

    fun register(name: String, email: String, password: String, phone: String, cpf: String, address: String) {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            users.register(name, email, password, phone, cpf, address).onSuccess {
                session.login(it.id, it.role)
                _state.value = UiState(loggedInRole = it.role)
            }.onFailure {
                _state.value = UiState(error = it.message)
            }
        }
    }

    companion object {
        fun create(app: RevivePartsApp) = AuthViewModel(app.userRepo, app.sessionRepo)
    }
}
