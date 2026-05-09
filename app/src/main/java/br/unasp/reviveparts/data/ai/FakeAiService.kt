package br.unasp.reviveparts.data.ai

import br.unasp.reviveparts.domain.model.RecognitionResult
import br.unasp.reviveparts.domain.model.SeedIds
import kotlinx.coroutines.delay

class FakeAiService(private val simulatedDelayMs: Long = 2500L) {
    suspend fun recognize(text: String?, imagePath: String?): RecognitionResult {
        delay(simulatedDelayMs)
        return RecognitionResult(
            productId = SeedIds.MANIVELA_VW,
            confidence = 0.92f,
            label = "Manivela de Vidro VW (reconhecida)"
        )
    }
}
