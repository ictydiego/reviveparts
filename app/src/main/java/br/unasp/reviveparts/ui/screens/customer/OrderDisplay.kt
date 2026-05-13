package br.unasp.reviveparts.ui.screens.customer

import br.unasp.reviveparts.R
import br.unasp.reviveparts.domain.model.OrderStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class StatusDisplay(val stage: Int, val label: String, val emoji: String)

fun OrderStatus.display(): StatusDisplay = when (this) {
    OrderStatus.PLACED, OrderStatus.IN_REVIEW -> StatusDisplay(0, "Aguardando produção", "🛒")
    OrderStatus.PRINTING, OrderStatus.PACKING -> StatusDisplay(1, "Imprimindo", "🏗️")
    OrderStatus.SHIPPED -> StatusDisplay(2, "A caminho", "🚚")
    OrderStatus.DELIVERED -> StatusDisplay(3, "Entregue", "✓")
}

val customerStageLabels = listOf(
    "Pagamento confirmado",
    "Impressão 3D em andamento",
    "Pedido enviado",
    "Pedido entregue"
)

val customerStageShort = listOf("Pago", "Impressão", "Envio", "Entrega")

fun formatOrderId(id: Long): String = "BR-%06d".format(200000 + id)

fun formatDateShort(epochMs: Long): String =
    SimpleDateFormat("dd' - 'MMM", Locale("pt", "BR")).format(Date(epochMs))

fun productImage(name: String): Int {
    val n = name.lowercase()
    return when {
        "manivela" in n -> R.drawable.manivela_vidro
        "maçaneta" in n || "macaneta" in n -> R.drawable.macaneta
        "botão" in n || "botao" in n -> R.drawable.botao
        "console" in n -> R.drawable.console_central
        "porta" in n || "puxador" in n -> R.drawable.porta_copos
        "luz" in n || "teto" in n -> R.drawable.luz_de_teto
        "soleira" in n -> R.drawable.soleira_opala
        "subaru" in n || "radiador" in n || "washer" in n -> R.drawable.tamparadiador
        "retrovisor" in n || "mirror" in n || "espelho" in n -> R.drawable.retrovisor_fiat
        else -> R.drawable.manivela_vidro
    }
}
