package br.unasp.reviveparts.domain.model

enum class Role { CUSTOMER, OWNER }

enum class OrderStatus(val label: String) {
    PLACED("Pedido feito"),
    IN_REVIEW("Em análise"),
    PRINTING("Imprimindo"),
    PACKING("Embalando"),
    SHIPPED("Saiu para entrega"),
    DELIVERED("Entregue");

    fun next(): OrderStatus? = entries.getOrNull(ordinal + 1)
    companion object { val pipeline: List<OrderStatus> = entries.toList() }
}

enum class PaymentType { CARD, PIX }
enum class OrderSource { CATALOG, AI }

data class RecognitionResult(
    val productId: Long,
    val confidence: Float,
    val label: String
)

object SeedIds { const val MANIVELA_VW: Long = 1L }
