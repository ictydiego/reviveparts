package br.unasp.reviveparts

import br.unasp.reviveparts.data.ai.FakeAiService
import br.unasp.reviveparts.domain.model.SeedIds
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FakeAiServiceTest {
    @Test fun alwaysReturnsManivela() = runTest {
        val r = FakeAiService(simulatedDelayMs = 0).recognize("uma manivela", null)
        assertEquals(SeedIds.MANIVELA_VW, r.productId)
    }
}
