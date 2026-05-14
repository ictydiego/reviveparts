package br.unasp.reviveparts.data.payments

import kotlinx.coroutines.delay

class PaymentSimulator {
    suspend fun chargeCard(numberDigits: String, totalCents: Long): Result<String> {
        delay(1500)
        val digits = numberDigits.filter { it.isDigit() }
        if (digits.length < 13) return Result.failure(IllegalArgumentException("Cartão inválido"))
        return Result.success("auth_${System.currentTimeMillis()}")
    }
}
