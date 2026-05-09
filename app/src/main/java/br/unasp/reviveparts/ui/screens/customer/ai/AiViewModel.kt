package br.unasp.reviveparts.ui.screens.customer.ai

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.ai.FakeAiService
import br.unasp.reviveparts.data.db.entities.ProductEntity
import br.unasp.reviveparts.data.db.entities.UserEntity
import br.unasp.reviveparts.data.repo.ProductRepository
import br.unasp.reviveparts.data.repo.SessionRepository
import br.unasp.reviveparts.data.repo.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class AiViewModel(
    private val ai: FakeAiService,
    private val products: ProductRepository,
    private val users: UserRepository,
    private val session: SessionRepository
) : ViewModel() {

    sealed interface UiState {
        data class Input(val text: String = DEFAULT_TEXT, val imageUri: Uri? = null) : UiState
        data class Analyzing(val imageUri: Uri?, val step: Int) : UiState
        data class Result(val product: ProductEntity, val confidence: Float) : UiState
        data class Model3D(val product: ProductEntity, val qty: Int) : UiState
        data class Error(val message: String) : UiState
    }

    val state = MutableStateFlow<UiState>(UiState.Input())
    val user = MutableStateFlow<UserEntity?>(null)

    init {
        viewModelScope.launch {
            session.current()?.let { s ->
                users.observeById(s.userId).collect { user.value = it }
            }
        }
    }

    fun setText(t: String) {
        (state.value as? UiState.Input)?.let { state.value = it.copy(text = t) }
    }

    fun setImage(uri: Uri?) {
        (state.value as? UiState.Input)?.let { state.value = it.copy(imageUri = uri) }
    }

    fun analyze() {
        val cur = state.value as? UiState.Input ?: return
        viewModelScope.launch {
            state.value = UiState.Analyzing(cur.imageUri, 0)
            delay(700); state.value = UiState.Analyzing(cur.imageUri, 1)
            delay(900); state.value = UiState.Analyzing(cur.imageUri, 2)
            delay(900); state.value = UiState.Analyzing(cur.imageUri, 3)
            delay(700)
            try {
                val r = ai.recognize(cur.text, cur.imageUri?.toString())
                val p = products.findById(r.productId)
                if (p == null) state.value = UiState.Error("Peça não cadastrada no catálogo")
                else state.value = UiState.Result(p, r.confidence)
            } catch (t: Throwable) {
                state.value = UiState.Error(t.message ?: "Erro")
            }
        }
    }

    fun toModel3D() {
        val r = state.value as? UiState.Result ?: return
        state.value = UiState.Model3D(r.product, qty = 1)
    }

    fun changeQty(delta: Int) {
        val m = state.value as? UiState.Model3D ?: return
        state.value = m.copy(qty = (m.qty + delta).coerceAtLeast(1))
    }

    fun reset() { state.value = UiState.Input() }

    fun back() {
        state.value = when (val s = state.value) {
            is UiState.Analyzing -> UiState.Input()
            is UiState.Result -> UiState.Input()
            is UiState.Model3D -> UiState.Result(s.product, 0.97f)
            is UiState.Error -> UiState.Input()
            else -> s
        }
    }

    companion object {
        const val DEFAULT_TEXT = "Preciso de uma peça para o fusca, onde essa peça tem 110mm de diâmetro, um furo de raio 3mm, essa peça é usada para rodar o mecanismo do vidro, e abrir e fechar o vidro"
        fun create(app: RevivePartsApp) = AiViewModel(app.aiService, app.productRepo, app.userRepo, app.sessionRepo)
    }
}
