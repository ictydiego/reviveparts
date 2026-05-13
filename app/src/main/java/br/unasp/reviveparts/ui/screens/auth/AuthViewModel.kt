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
        val loggedInRole: Role? = null,
        val biometricReady: Boolean = false
    )

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            session.biometricSession.collect { saved ->
                _state.value = _state.value.copy(
                    biometricReady = saved != null && users.hasActiveFirebaseUser(saved.firebaseUid)
                )
            }
        }
    }

    fun login(email: String, password: String) {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            val r = users.login(email, password)
            r.onSuccess {
                session.login(it.id, it.role, it.firebaseUid)
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
                session.login(it.id, it.role, it.firebaseUid)
                _state.value = UiState(loggedInRole = it.role)
            }.onFailure {
                _state.value = UiState(error = it.message)
            }
        }
    }

    fun loginWithBiometric() {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            val restored = session.restoreBiometricSession()
            if (restored == null || !users.hasActiveFirebaseUser(restored.firebaseUid)) {
                session.logout()
                _state.value = UiState(error = "Entre com e-mail e senha para ativar a biometria novamente")
            } else {
                _state.value = UiState(loggedInRole = restored.role, biometricReady = true)
            }
        }
    }

    companion object {
        fun create(app: RevivePartsApp) = AuthViewModel(app.userRepo, app.sessionRepo)
    }
}
