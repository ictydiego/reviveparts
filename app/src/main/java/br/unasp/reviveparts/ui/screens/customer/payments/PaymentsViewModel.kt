package br.unasp.reviveparts.ui.screens.customer.payments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.unasp.reviveparts.RevivePartsApp
import br.unasp.reviveparts.data.db.entities.CardEntity
import br.unasp.reviveparts.data.repo.CardRepository
import br.unasp.reviveparts.data.repo.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PaymentsViewModel(
    private val cards: CardRepository,
    private val session: SessionRepository
) : ViewModel() {
    val items = MutableStateFlow<List<CardEntity>>(emptyList())
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    private var userId: Long = 0L

    init {
        viewModelScope.launch {
            val s = session.current() ?: return@launch
            userId = s.userId
            cards.observeForUser(userId).collect { items.value = it }
        }
    }

    fun add(numberRaw: String, holder: String, expiry: String, cvv: String, makeDefault: Boolean): Boolean {
        val digits = numberRaw.filter { it.isDigit() }
        if (digits.length < 13) { _error.value = "Digite ao menos 13 dígitos"; return false }
        if (holder.isBlank()) { _error.value = "Informe o nome impresso"; return false }
        if (!expiry.matches(Regex("^\\d{2}/\\d{2}$"))) { _error.value = "Validade no formato MM/AA"; return false }
        if (cvv.length !in 3..4 || !cvv.all { it.isDigit() }) { _error.value = "CVV inválido"; return false }
        _error.value = null
        viewModelScope.launch {
            cards.add(
                CardEntity(
                    userId = userId,
                    holderName = holder.trim(),
                    last4 = digits.takeLast(4),
                    brand = detectBrand(digits),
                    expiry = expiry
                ),
                makeDefault = makeDefault
            )
        }
        return true
    }

    fun delete(c: CardEntity) = viewModelScope.launch { cards.delete(c) }
    fun setDefault(c: CardEntity) = viewModelScope.launch { cards.setDefault(c) }
    fun clearError() { _error.value = null }

    private fun detectBrand(d: String): String = when {
        d.startsWith("4") -> "Visa"
        d.startsWith("5") -> "Mastercard"
        d.startsWith("3") -> "Amex"
        d.startsWith("6") -> "Elo"
        else -> "Cartão"
    }

    companion object {
        fun create(app: RevivePartsApp) = PaymentsViewModel(app.cardRepo, app.sessionRepo)
    }
}
