package br.unasp.reviveparts.data.payments

import kotlinx.coroutines.delay

class PaymentSimulator {
    suspend fun chargeCard(numberDigits: String, totalCents: Long): Result<String> {
        delay(1500)
        if (!LuhnValidator.isValid(numberDigits)) return Result.failure(IllegalArgumentException("Cartão inválido"))
        return Result.success("auth_${System.currentTimeMillis()}")
    }
}
