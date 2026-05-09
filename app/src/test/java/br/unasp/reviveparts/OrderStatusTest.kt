package br.unasp.reviveparts

import br.unasp.reviveparts.domain.model.OrderStatus
import org.junit.Assert.*
import org.junit.Test

class OrderStatusTest {
    @Test fun nextFromPlacedIsInReview() = assertEquals(OrderStatus.IN_REVIEW, OrderStatus.PLACED.next())
    @Test fun nextFromShippedIsDelivered() = assertEquals(OrderStatus.DELIVERED, OrderStatus.SHIPPED.next())
    @Test fun nextFromDeliveredIsNull() = assertNull(OrderStatus.DELIVERED.next())
    @Test fun pipelineHasSixSteps() = assertEquals(6, OrderStatus.pipeline.size)
}
