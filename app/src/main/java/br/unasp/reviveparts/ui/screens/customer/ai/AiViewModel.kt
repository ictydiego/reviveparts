package br.unasp.reviveparts.ui.screens.customer.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.ai.FakeAiService
import br.unasp.reviveparts.data.repo.ProductRepository
import br.unasp.reviveparts.data.db.entities.ProductEntity
import br.unasp.reviveparts.domain.model.RecognitionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AiViewModel(private val ai: FakeAiService, private val products: ProductRepository) : ViewModel() {
    sealed interface UiState {
        object Idle : UiState
        object Recognizing : UiState
        data class Result(val recognition: RecognitionResult, val product: ProductEntity) : UiState
        data class Error(val message: String) : UiState
    }
    private val _state = MutableStateFlow<UiState>(UiState.Idle)
    val state = _state.asStateFlow()

    fun recognize(text: String, imagePath: String?) {
        _state.value = UiState.Recognizing
        viewModelScope.launch {
            try {
                val r = ai.recognize(text, imagePath)
                val p = products.findById(r.productId) ?: return@launch run {
                    _state.value = UiState.Error("Peça não encontrada no catálogo")
                }
                _state.value = UiState.Result(r, p)
            } catch (t: Throwable) { _state.value = UiState.Error(t.message ?: "Erro") }
        }
    }
    fun reset() { _state.value = UiState.Idle }

    companion object { fun create(app: RevivePartsApp) = AiViewModel(app.aiService, app.productRepo) }
}
